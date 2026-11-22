"""Manejo de archivos de las señas: imagen y video (se guardan en /media)."""
import uuid

from django.conf import settings
from django.core.exceptions import ValidationError
from django.core.files.storage import default_storage

from .models import VideoReferencia

EXTENSIONES_IMAGEN = ('jpg', 'jpeg', 'png')
MAX_IMAGEN_MB = 2


def validar_imagen(archivo):
    ext = archivo.name.rsplit('.', 1)[-1].lower()
    if ext not in EXTENSIONES_IMAGEN:
        raise ValidationError('La imagen debe ser JPG o PNG.')
    if archivo.size > MAX_IMAGEN_MB * 1024 * 1024:
        raise ValidationError(f'La imagen supera el máximo de {MAX_IMAGEN_MB} MB.')


def guardar_imagen(archivo):
    """Guarda la imagen y devuelve la ruta que se almacena en sena.imagen."""
    ext = archivo.name.rsplit('.', 1)[-1].lower()
    ruta = default_storage.save(f'imagenes_senas/{uuid.uuid4()}.{ext}', archivo)
    return settings.MEDIA_URL + ruta


def guardar_video(sena, archivo):
    """Guarda el video y su ruta en la tabla video_referencia."""
    ruta = default_storage.save(f'videos_senas/{uuid.uuid4()}.mp4', archivo)
    VideoReferencia.objects.create(
        sena=sena,
        url_video=settings.MEDIA_URL + ruta,
        formato='mp4',
        tamano_mb=round(archivo.size / (1024 * 1024), 2),
    )


def borrar_archivo(url):
    """Borra un archivo de /media a partir de la ruta guardada (si existe)."""
    if url and url.startswith(settings.MEDIA_URL):
        default_storage.delete(url[len(settings.MEDIA_URL):])