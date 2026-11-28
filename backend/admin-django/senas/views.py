from django.core.exceptions import ValidationError
from django.db import IntegrityError, transaction
from django.db.models import Count, Q
from rest_framework import viewsets, status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from rest_framework.permissions import AllowAny
from rest_framework.response import Response

from .archivos import borrar_archivo, guardar_imagen, guardar_video, validar_imagen
from .auth import crear_token, es_admin, verificar_password
from .entrenamiento import puntos_a_texto, secuencia_a_texto
from .models import MuestraSena, MuestraSecuencia, RegistroUso, Sena, Usuario, validar_video
from .progreso import META_MOVIMIENTOS, META_MUESTRAS
from .serializers import RegistroUsoSerializer, SenaSerializer
from .services import KeypointProcessorService


class SenaViewSet(viewsets.ModelViewSet):
    serializer_class = SenaSerializer
    parser_classes = (MultiPartParser, FormParser, JSONParser)

    def get_queryset(self):
        qs = (
            Sena.objects.select_related('categoria_ref')
            .prefetch_related('videos')
            .annotate(
                _muestras=Count('muestras', distinct=True),
                _secuencias=Count('secuencias', distinct=True),
            )
        )
        p = self.request.query_params
        if p.get('categoria'):
            qs = qs.filter(categoria_ref_id=p['categoria'])
        if p.get('q'):
            q = p['q'].strip()
            qs = qs.filter(Q(nombre__icontains=q) | Q(descripcion__icontains=q))
        if p.get('estado') == 'entrenada':
            qs = qs.filter(Q(_muestras__gte=META_MUESTRAS) | Q(_secuencias__gte=META_MOVIMIENTOS))
        elif p.get('estado') == 'sin_entrenar':
            qs = qs.filter(_muestras__lt=META_MUESTRAS, _secuencias__lt=META_MOVIMIENTOS)
        return qs.order_by('-fecha_registro')

    def _error(self, errores, codigo=status.HTTP_400_BAD_REQUEST):
        return Response({"exito": False, "errores": errores}, status=codigo)

    def _validar_archivos(self, request, imagen_obligatoria, video_obligatorio=False):
        """Devuelve (imagen, video, errores)."""
        errores = {}
        imagen = request.FILES.get('imagen')
        video = request.FILES.get('video')
        if imagen:
            try:
                validar_imagen(imagen)
            except ValidationError as e:
                errores['imagen'] = e.messages
        elif imagen_obligatoria:
            errores['imagen'] = ['CA-04: La imagen es obligatoria.']
        if video:
            try:
                validar_video(video)
            except ValidationError as e:
                errores['video'] = e.messages
        elif video_obligatorio:
            errores['video'] = ['El video de referencia es obligatorio: sirve para recordar cómo se hace la seña.']
        return imagen, video, errores

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        valido = serializer.is_valid()
        imagen, video, errores_archivos = self._validar_archivos(
            request, imagen_obligatoria=True, video_obligatorio=True)
        if not valido or errores_archivos:
            return self._error({**serializer.errors, **errores_archivos})

        with transaction.atomic():
            sena = serializer.save(imagen=guardar_imagen(imagen))
            if video:
                guardar_video(sena, video)

        return Response({
            "exito": True,
            "mensaje": "Seña registrada correctamente en el sistema.",
            "data": self.get_serializer(self.get_queryset().get(pk=sena.pk)).data
        }, status=status.HTTP_201_CREATED)

    def update(self, request, *args, **kwargs):
        partial = kwargs.pop('partial', False)
        instance = self.get_object()
        serializer = self.get_serializer(instance, data=request.data, partial=partial)
        valido = serializer.is_valid()
        # Si la seña aún no tiene video, al editarla se pide uno
        imagen, video, errores_archivos = self._validar_archivos(
            request, imagen_obligatoria=False, video_obligatorio=not instance.videos.exists())
        if not valido or errores_archivos:
            return self._error({**serializer.errors, **errores_archivos})

        imagen_vieja = instance.imagen
        with transaction.atomic():
            if imagen:
                sena = serializer.save(imagen=guardar_imagen(imagen))
            else:
                sena = serializer.save()
            if video:
                sena.videos.all().delete()  # el nuevo video reemplaza al anterior
                guardar_video(sena, video)
        if imagen:
            borrar_archivo(imagen_vieja)

        return Response({
            "exito": True,
            "mensaje": "Seña actualizada correctamente.",
            "data": self.get_serializer(self.get_queryset().get(pk=sena.pk)).data
        }, status=status.HTTP_200_OK)

    def destroy(self, request, *args, **kwargs):
        instance = self.get_object()
        imagen = instance.imagen
        try:
            self.perform_destroy(instance)
        except IntegrityError:
            return self._error(
                {"sena": ["No se puede eliminar: la seña tiene registros asociados."]},
                codigo=status.HTTP_409_CONFLICT,
            )
        borrar_archivo(imagen)
        return Response({
            "exito": True,
            "mensaje": "Seña eliminada correctamente del sistema."
        }, status=status.HTTP_200_OK)


class RegistroUsoViewSet(viewsets.ModelViewSet):
    queryset = RegistroUso.objects.all().order_by('-fecha_hora')
    serializer_class = RegistroUsoSerializer


@api_view(['POST'])
def predecir_sena(request):
    landmarks = request.data.get('landmarks', [])

    if not isinstance(landmarks, list) or len(landmarks) != 21:
        return Response({
            "exito": False,
            "error": "Se requieren exactamente 21 puntos (keypoints) con coordenadas (x, y, z)."
        }, status=status.HTTP_400_BAD_REQUEST)

    try:
        vector_features = KeypointProcessorService.preprocesar_landmarks(landmarks)
        resultado = KeypointProcessorService.predecir_sena(vector_features)

        nombre_detectado = resultado.get("sena_detectada", "Desconocido")
        sena_obj = Sena.objects.filter(nombre__iexact=nombre_detectado).first()
        RegistroUso.objects.create(sena=sena_obj, nombre_sena=nombre_detectado)

        return Response({"exito": True, "data": resultado}, status=status.HTTP_200_OK)

    except Exception as e:
        return Response({"exito": False, "error": str(e)},
                        status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['POST'])
@permission_classes([AllowAny])
def login_admin(request):
    email = (request.data.get('email') or '').strip()
    password = request.data.get('password') or ''

    usuario = Usuario.objects.filter(email__iexact=email).first()
    if not usuario or not verificar_password(password, usuario.password_hash):
        return Response({"exito": False, "error": "Correo o contraseña incorrectos"},
                        status=status.HTTP_401_UNAUTHORIZED)
    if not usuario.activo:
        return Response({"exito": False, "error": "Tu cuenta está desactivada."},
                        status=status.HTTP_403_FORBIDDEN)
    if not es_admin(usuario):
        return Response({"exito": False, "error": "Esta cuenta no tiene permisos de administrador."},
                        status=status.HTTP_403_FORBIDDEN)

    return Response({
        "exito": True,
        "token": crear_token(usuario),
        "usuario": {"nombre": usuario.nombre, "email": usuario.email}
    })


def _puntos_validos(puntos):
    return (
        isinstance(puntos, list) and len(puntos) in (21, 42)
        and all(isinstance(p, dict) and {'x', 'y', 'z'} <= p.keys() for p in puntos)
    )


@api_view(['POST'])
def guardar_muestra(request, id_sena):
    sena = Sena.objects.filter(pk=id_sena).first()
    if not sena:
        return Response({"exito": False, "error": "La seña no existe."}, status=status.HTTP_404_NOT_FOUND)
    puntos = request.data
    if not _puntos_validos(puntos):
        return Response({"exito": False, "error": "Se esperan 21 puntos (una mano) o 42 (dos manos)."},
                        status=status.HTTP_400_BAD_REQUEST)
    MuestraSena.objects.create(sena=sena, puntos=puntos_a_texto(puntos))
    return Response({"exito": True, "mensaje": "Muestra guardada."}, status=status.HTTP_201_CREATED)


@api_view(['POST'])
def guardar_secuencia(request, id_sena):
    sena = Sena.objects.filter(pk=id_sena).first()
    if not sena:
        return Response({"exito": False, "error": "La seña no existe."}, status=status.HTTP_404_NOT_FOUND)
    cuadros = request.data
    if not isinstance(cuadros, list) or not 8 <= len(cuadros) <= 80:
        return Response({"exito": False, "error": "Se esperan entre 8 y 80 cuadros."},
                        status=status.HTTP_400_BAD_REQUEST)
    if not all(_puntos_validos(c) and len(c) == len(cuadros[0]) for c in cuadros):
        return Response({"exito": False, "error": "Todos los cuadros deben tener 21 o 42 puntos, del mismo tamaño."},
                        status=status.HTTP_400_BAD_REQUEST)
    MuestraSecuencia.objects.create(sena=sena, cuadros=secuencia_a_texto(cuadros), num_cuadros=len(cuadros))
    return Response({"exito": True, "mensaje": "Movimiento guardado."}, status=status.HTTP_201_CREATED)


@api_view(['GET'])
def resumen_entrenamiento(request):
    senas = Sena.objects.annotate(
        n_muestras=Count('muestras', distinct=True),
        n_secuencias=Count('secuencias', distinct=True),
    ).order_by('nombre')
    return Response([
        {"id": s.id_sena, "nombre": s.nombre,
         "muestras": s.n_muestras, "secuencias": s.n_secuencias}
        for s in senas
    ])


@api_view(['DELETE'])
def borrar_entrenamiento(request, id_sena):
    sena = Sena.objects.filter(pk=id_sena).first()
    if not sena:
        return Response({"exito": False, "error": "La seña no existe."},
                        status=status.HTTP_404_NOT_FOUND)
    tipo = request.query_params.get('tipo', 'todas')  # muestras | secuencias | todas
    if tipo in ('muestras', 'todas'):
        sena.muestras.all().delete()
    if tipo in ('secuencias', 'todas'):
        sena.secuencias.all().delete()
    return Response({"exito": True, "mensaje": "Entrenamiento eliminado correctamente."})