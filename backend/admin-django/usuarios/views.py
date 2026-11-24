from rest_framework import serializers, status, viewsets
from rest_framework.decorators import api_view, parser_classes
from rest_framework.parsers import FormParser, MultiPartParser
from rest_framework.response import Response

from senas.auth import admin_de

from .perfil import (
    ErrorPerfil, cambiar_foto, cambiar_password, datos_perfil, quitar_foto,
)
from .serializers import UsuarioSerializer
from .services import cambiar_estado, filtrar, usuarios_de_la_app


class UsuarioViewSet(viewsets.ModelViewSet):
    """Lista de usuarios y activar/desactivar su cuenta (PATCH {"is_active": true|false})."""
    serializer_class = UsuarioSerializer
    http_method_names = ['get', 'patch', 'head', 'options']

    def get_queryset(self):
        p = self.request.query_params
        return filtrar(usuarios_de_la_app(), texto=p.get('q'), estado=p.get('estado'))

    def partial_update(self, request, *args, **kwargs):
        usuario = self.get_object()
        try:
            activo = serializers.BooleanField().run_validation(request.data.get('is_active'))
        except serializers.ValidationError:
            return Response({"exito": False, "error": "Envía is_active como true o false."},
                            status=status.HTTP_400_BAD_REQUEST)
        cambiar_estado(usuario, activo)
        return Response({
            "exito": True,
            "mensaje": f"Cuenta {'activada' if activo else 'desactivada'} correctamente.",
            "data": self.get_serializer(usuario).data,
        })


def _rechazo(error):
    return Response({"exito": False, "error": error.mensaje, "campo": error.campo},
                    status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
def perfil(request):
    """Datos del administrador con sesión. Solo puede cambiar su foto y su contraseña."""
    return Response(datos_perfil(admin_de(request), request))


@api_view(['POST', 'DELETE'])
@parser_classes([MultiPartParser, FormParser])
def perfil_foto(request):
    """Agrega o cambia la foto de perfil (POST, campo 'foto') o la quita (DELETE)."""
    admin = admin_de(request)
    if request.method == 'DELETE':
        quitar_foto(admin)
        mensaje = "Foto eliminada correctamente."
    else:
        try:
            cambiar_foto(admin, request.FILES.get('foto'))
        except ErrorPerfil as error:
            return _rechazo(error)
        mensaje = "Foto actualizada correctamente."
    return Response({"exito": True, "mensaje": mensaje, "data": datos_perfil(admin, request)})


@api_view(['POST'])
def perfil_password(request):
    """Cambia la contraseña del administrador: pide la actual, la nueva y su confirmación."""
    admin = admin_de(request)
    try:
        cambiar_password(
            admin,
            request.data.get('actual'),
            request.data.get('nueva'),
            request.data.get('confirmar'),
        )
    except ErrorPerfil as error:
        return _rechazo(error)
    return Response({"exito": True, "mensaje": "Contraseña actualizada correctamente."})
