"""Reglas de negocio de los usuarios (separadas de las vistas)."""
from django.db.models import Q

from senas.auth import ROLES_ADMIN
from senas.models import Usuario


def usuarios_de_la_app():
    """Usuarios que se registran en la web o la app (los administradores no se listan)."""
    qs = Usuario.objects.all()
    for rol in ROLES_ADMIN:
        qs = qs.exclude(rol__iexact=rol)
    return qs.order_by('id_usuario')


def filtrar(qs, texto=None, estado=None):
    if texto:
        texto = texto.strip()
        qs = qs.filter(Q(nombre__icontains=texto) | Q(email__icontains=texto))
    if estado == 'activo':
        qs = qs.filter(activo=True)
    elif estado == 'inactivo':
        qs = qs.filter(activo=False)
    return qs


def cambiar_estado(usuario, activo):
    usuario.activo = activo
    usuario.save(update_fields=['activo'])
    return usuario