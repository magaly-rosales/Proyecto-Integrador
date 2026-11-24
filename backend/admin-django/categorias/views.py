from django.core.exceptions import ValidationError
from django.db.models import Count
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.parsers import FormParser, JSONParser, MultiPartParser
from rest_framework.response import Response

from senas.archivos import borrar_archivo, guardar_imagen, validar_imagen
from senas.models import Categoria

from .serializers import CategoriaSerializer, SenaDeCategoriaSerializer
from .services import eliminar_categoria, guardar_categoria


class CategoriaViewSet(viewsets.ModelViewSet):
    serializer_class = CategoriaSerializer
    parser_classes = (MultiPartParser, FormParser, JSONParser)

    def get_queryset(self):
        return Categoria.objects.annotate(_total=Count('senas')).order_by('nombre')

    def _error(self, errores):
        return Response({'exito': False, 'errores': errores}, status=status.HTTP_400_BAD_REQUEST)

    def _validar_imagen(self, request, obligatoria):
        """Devuelve (imagen, errores)."""
        errores = {}
        imagen = request.FILES.get('imagen')
        if imagen:
            try:
                validar_imagen(imagen)
            except ValidationError as e:
                errores['imagen'] = e.messages
        elif obligatoria:
            errores['imagen'] = ['La imagen es obligatoria.']
        return imagen, errores

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        valido = serializer.is_valid()
        imagen, errores_imagen = self._validar_imagen(request, obligatoria=True)
        if not valido or errores_imagen:
            return self._error({**serializer.errors, **errores_imagen})

        categoria = guardar_categoria(serializer, guardar_imagen(imagen))
        return Response({
            'exito': True,
            'mensaje': 'Categoría creada correctamente.',
            'data': self.get_serializer(categoria).data,
        }, status=status.HTTP_201_CREATED)

    def update(self, request, *args, **kwargs):
        partial = kwargs.pop('partial', False)
        instance = self.get_object()
        serializer = self.get_serializer(instance, data=request.data, partial=partial)
        valido = serializer.is_valid()
        imagen, errores_imagen = self._validar_imagen(request, obligatoria=False)
        if not valido or errores_imagen:
            return self._error({**serializer.errors, **errores_imagen})

        imagen_vieja = instance.imagen
        categoria = guardar_categoria(serializer, guardar_imagen(imagen) if imagen else None)
        if imagen:
            borrar_archivo(imagen_vieja)
        return Response({
            'exito': True,
            'mensaje': 'Categoría actualizada correctamente.',
            'data': self.get_serializer(categoria).data,
        })

    def destroy(self, request, *args, **kwargs):
        total = eliminar_categoria(self.get_object())
        mensaje = 'Categoría eliminada correctamente.'
        if total:
            mensaje = f'Categoría eliminada junto con sus {total} seña(s).'
        return Response({'exito': True, 'mensaje': mensaje, 'senas_eliminadas': total})

    @action(detail=True, methods=['get'])
    def senas(self, request, pk=None):
        """GET /api/categorias/<id>/senas/ -> señas de esa categoría."""
        categoria = self.get_object()
        senas = categoria.senas.order_by('nombre')
        return Response(SenaDeCategoriaSerializer(senas, many=True, context={'request': request}).data)