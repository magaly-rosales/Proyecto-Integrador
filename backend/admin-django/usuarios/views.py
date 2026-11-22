from rest_framework import serializers, status, viewsets
from rest_framework.response import Response

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