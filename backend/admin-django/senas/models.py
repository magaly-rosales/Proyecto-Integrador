import os
import uuid
from django.db import models
from django.core.exceptions import ValidationError

def renombrar_y_validar_video(instance, filename):
    # CA-03: Validar formato MP4
    ext = filename.split('.')[-1].lower()
    if ext != 'mp4':
        raise ValidationError('CA-03: Formato no permitido. El archivo debe ser estrictamente un video MP4.')

    # Generar un nombre único para evitar sobreescritura de archivos
    nuevo_nombre = f"{uuid.uuid4()}.mp4"
    return os.path.join('videos_senas/', nuevo_nombre)

def validar_tamano_video(file):
    # CA-03: Límite máximo 50MB (50 * 1024 * 1024 bytes)
    max_size = 50 * 1024 * 1024
    if file.size > max_size:
        raise ValidationError('CA-03: El tamaño del video excede el límite máximo permitido de 50MB.')

class Sena(models.Model):
    # CA-04: Nombre y Categoría no nulos y no vacíos
    nombre = models.CharField(max_length=100, null=False, blank=False)
    categoria = models.CharField(max_length=50, null=False, blank=False)
    descripcion = models.TextField(blank=True, null=True)
    video = models.FileField(
        upload_to=renombrar_y_validar_video,
        validators=[validar_tamano_video]
    )
    fecha_creacion = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "Seña"
        verbose_name_plural = "Señas"
        ordering = ['-fecha_creacion']

    def __str__(self):
        return f"{self.nombre} ({self.categoria})"