"""Reglas del perfil del administrador: solo puede cambiar su foto y su contraseña."""
import bcrypt
from django.core.exceptions import ValidationError

from senas.archivos import borrar_archivo, guardar_foto_perfil, validar_imagen
from senas.auth import verificar_password

from .serializers import UsuarioSerializer

MIN_PASSWORD = 8


class ErrorPerfil(Exception):
    """Dato inválido: guarda el campo y el mensaje que se muestra al administrador."""

    def __init__(self, campo, mensaje):
        super().__init__(mensaje)
        self.campo = campo
        self.mensaje = mensaje


def datos_perfil(usuario, request=None):
    palabras = usuario.nombre.split()
    return {
        'nombre': usuario.nombre,
        'email': usuario.email,
        'rol': 'Administrador',
        'iniciales': ''.join(p[0] for p in palabras[:2]).upper(),
        'foto': UsuarioSerializer(context={'request': request}).get_foto(usuario),
    }


def validar_password_nueva(actual, nueva, confirmar):
    if len(nueva) < MIN_PASSWORD:
        raise ErrorPerfil('nueva', f'La nueva contraseña debe tener al menos {MIN_PASSWORD} caracteres.')
    if not any(c.isalpha() for c in nueva):
        raise ErrorPerfil('nueva', 'La nueva contraseña debe tener al menos una letra.')
    if not any(c.isdigit() for c in nueva):
        raise ErrorPerfil('nueva', 'La nueva contraseña debe tener al menos un número.')
    if nueva == actual:
        raise ErrorPerfil('nueva', 'La nueva contraseña debe ser distinta de la actual.')
    if nueva != confirmar:
        raise ErrorPerfil('confirmar', 'La confirmación no coincide con la nueva contraseña.')


def cambiar_foto(usuario, archivo):
    """Guarda la foto nueva y borra la anterior."""
    if archivo is None:
        raise ErrorPerfil('foto', 'Elige una imagen.')
    try:
        validar_imagen(archivo)
    except ValidationError as error:
        raise ErrorPerfil('foto', error.messages[0])
    anterior = usuario.foto
    usuario.foto = guardar_foto_perfil(archivo)
    usuario.save(update_fields=['foto'])
    borrar_archivo(anterior)
    return usuario


def quitar_foto(usuario):
    anterior = usuario.foto
    usuario.foto = None
    usuario.save(update_fields=['foto'])
    borrar_archivo(anterior)
    return usuario


def cambiar_password(usuario, actual, nueva, confirmar):
    actual, nueva, confirmar = actual or '', nueva or '', confirmar or ''
    if not verificar_password(actual, usuario.password_hash):
        raise ErrorPerfil('actual', 'La contraseña actual no es correcta.')
    validar_password_nueva(actual, nueva, confirmar)
    usuario.password_hash = bcrypt.hashpw(nueva.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
    usuario.save(update_fields=['password_hash'])
