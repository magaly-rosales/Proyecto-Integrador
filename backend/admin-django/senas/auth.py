import bcrypt
from django.core import signing
from rest_framework.permissions import BasePermission

from .models import Usuario

SAL = 'admin-token'
DURACION_S = 8 * 60 * 60  # la sesión del admin dura 8 horas
ROLES_ADMIN = ('admin', 'administrador')


def es_admin(usuario):
    return usuario.rol.lower() in ROLES_ADMIN


def verificar_password(password, password_hash):
    
    try:
        return bcrypt.checkpw(password.encode('utf-8'), password_hash.encode('utf-8'))
    except ValueError:
        return False


def crear_token(usuario):
    return signing.dumps({'id': usuario.id_usuario}, salt=SAL)


class EsAdmin(BasePermission):
    message = 'Sesión de administrador no válida o vencida.'

    def has_permission(self, request, view):
        cabecera = request.headers.get('Authorization', '')
        if not cabecera.startswith('Bearer '):
            return False
        try:
            datos = signing.loads(cabecera[7:], salt=SAL, max_age=DURACION_S)
        except signing.BadSignature:
            return False
        usuario = Usuario.objects.filter(pk=datos.get('id')).first()
        return bool(usuario and usuario.activo and es_admin(usuario))