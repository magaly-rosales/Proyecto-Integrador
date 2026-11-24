from rest_framework import serializers, status
from rest_framework.decorators import api_view
from rest_framework.response import Response

from usuarios.services import cambiar_estado, usuarios_de_la_app

from . import servicio
from .gemini import ErrorGemini


@api_view(['POST'])
def preguntar(request):
    """POST /api/asistente/ {"mensajes": [{"rol": "usuario"|"asistente", "texto": "..."}]}"""
    try:
        resultado = servicio.responder(request.data.get('mensajes'))
    except ValueError as error:
        return Response({"exito": False, "error": str(error)}, status=status.HTTP_400_BAD_REQUEST)
    except ErrorGemini as error:
        return Response({"exito": False, "error": error.mensaje}, status=error.estado)
    return Response({"exito": True, **resultado})


@api_view(['POST'])
def confirmar_accion(request):
    """POST /api/asistente/accion/ {"id_usuario": 5, "activar": true}: el administrador confirmó el cambio."""
    try:
        activar = serializers.BooleanField().run_validation(request.data.get('activar'))
        id_usuario = serializers.IntegerField().run_validation(request.data.get('id_usuario'))
    except serializers.ValidationError:
        return Response({"exito": False, "error": "Envía id_usuario (número) y activar (true o false)."},
                        status=status.HTTP_400_BAD_REQUEST)

    usuario = usuarios_de_la_app().filter(pk=id_usuario).first()
    if not usuario:
        return Response({"exito": False, "error": "El usuario no existe."}, status=status.HTTP_404_NOT_FOUND)

    cambiar_estado(usuario, activar)
    return Response({
        "exito": True,
        "mensaje": f"La cuenta de {usuario.nombre} fue {'activada' if activar else 'desactivada'}.",
        "data": {"id": usuario.id_usuario, "is_active": usuario.activo},
    })
