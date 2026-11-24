"""Ayudas compartidas por las pruebas de todas las apps."""
import shutil
import tempfile

import bcrypt
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import override_settings
from rest_framework.test import APIClient

from senas.auth import crear_token
from senas.models import Usuario

# PNG válido de 1x1 píxel
PNG_1X1 = (
    b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15\xc4'
    b'\x89\x00\x00\x00\rIDATx\x9cc\xf8\xff\xff?\x00\x05\xfe\x02\xfe\xa7\x9a\xa0\xa0\x00\x00\x00\x00IEND\xaeB`\x82'
)


def imagen_png(nombre='foto.png'):
    return SimpleUploadedFile(nombre, PNG_1X1, content_type='image/png')


def video_mp4(nombre='video.mp4'):
    return SimpleUploadedFile(nombre, b'\x00\x00\x00\x18ftypmp42' + b'0' * 64, content_type='video/mp4')


def crear_usuario(nombre='Ana Torres', email='ana@correo.com', rol='usuario', activo=True, password='clave123'):
    clave = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt(rounds=4)).decode('utf-8')
    return Usuario.objects.create(nombre=nombre, email=email, password_hash=clave, rol=rol, activo=activo)


def crear_admin():
    return crear_usuario('Administrador', 'admin@senasapp.pe', rol='admin', password='admin123')


def cliente_de(usuario):
    """Cliente de pruebas con la sesión de ese usuario (token Bearer, igual que el panel)."""
    cliente = APIClient()
    cliente.credentials(HTTP_AUTHORIZATION=f'Bearer {crear_token(usuario)}')
    return cliente


class CarpetaMediaTemporal:
    """Mixin: guarda los archivos subidos en una carpeta temporal y la borra al terminar."""

    @classmethod
    def setUpClass(cls):
        cls._media = tempfile.mkdtemp(prefix='senasapp_media_')
        cls._override = override_settings(MEDIA_ROOT=cls._media)
        cls._override.enable()
        super().setUpClass()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()
        cls._override.disable()
        shutil.rmtree(cls._media, ignore_errors=True)


def crear_categoria(nombre='Saludos', color='#2BBCC9', imagen=None):
    from senas.models import Categoria
    return Categoria.objects.create(nombre=nombre, color=color, imagen=imagen)


def crear_sena(nombre='Hola', categoria=None, imagen=None, descripcion=''):
    from senas.models import Sena
    return Sena.objects.create(
        nombre=nombre,
        categoria=categoria.nombre if categoria else '',
        categoria_ref=categoria,
        imagen=imagen,
        descripcion=descripcion,
    )


def ruta_en_disco(url):
    """Ruta real en disco de un archivo guardado como '/media/…'."""
    import os
    from django.conf import settings
    return os.path.join(settings.MEDIA_ROOT, url[len(settings.MEDIA_URL):])


def puntos_mano(desplazamiento=0.0):
    """21 puntos (x, y, z) de una mano, como los envía el navegador."""
    return [{'x': i / 40 + desplazamiento, 'y': i / 30, 'z': i / 50} for i in range(21)]
