from django.db import models
from django.core.exceptions import ValidationError


def validar_video(file):
    ext = file.name.split('.')[-1].lower()
    if ext != 'mp4':
        raise ValidationError('CA-03: Formato no permitido. El archivo debe ser estrictamente un video MP4.')
    if file.size > 50 * 1024 * 1024:
        raise ValidationError('CA-03: El tamaño del video excede el límite máximo permitido de 50MB.')


class Categoria(models.Model):
    id_categoria = models.AutoField(primary_key=True)
    nombre = models.CharField(max_length=50, unique=True)
    color = models.CharField(max_length=7, default='#2BBCC9')
    icono = models.CharField(max_length=30, default='hand')
    fecha_registro = models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = False
        db_table = 'categoria'
        ordering = ['nombre']

    def __str__(self):
        return self.nombre


class Sena(models.Model):
    id_sena = models.AutoField(primary_key=True)
    nombre = models.CharField(max_length=100)
    categoria = models.CharField(max_length=50)   # texto antiguo: lo sigue leyendo Spring Boot
    categoria_ref = models.ForeignKey(Categoria, on_delete=models.SET_NULL, db_column='id_categoria',
                                      null=True, blank=True, related_name='senas')
    imagen = models.CharField(max_length=255, blank=True, null=True)
    descripcion = models.TextField(blank=True, null=True)
    fecha_registro = models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = False
        db_table = 'sena'
        ordering = ['-fecha_registro']

    def __str__(self):
        return f"{self.nombre} ({self.categoria})"


class VideoReferencia(models.Model):
    id_video = models.AutoField(primary_key=True)
    sena = models.ForeignKey(Sena, on_delete=models.CASCADE, db_column='id_sena', related_name='videos')
    url_video = models.CharField(max_length=255)
    formato = models.CharField(max_length=10, default='mp4')
    tamano_mb = models.DecimalField(max_digits=5, decimal_places=2, null=True, blank=True)
    fecha_subida = models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = False
        db_table = 'video_referencia'


class MuestraSena(models.Model):
    id_muestra = models.AutoField(primary_key=True)
    sena = models.ForeignKey(Sena, on_delete=models.CASCADE, db_column='id_sena', related_name='muestras')
    puntos = models.TextField()
    fecha_registro = models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = False
        db_table = 'muestra_sena'


class MuestraSecuencia(models.Model):
    id_secuencia = models.AutoField(primary_key=True)
    sena = models.ForeignKey(Sena, on_delete=models.CASCADE, db_column='id_sena', related_name='secuencias')
    cuadros = models.TextField()
    num_cuadros = models.IntegerField()
    fecha_registro = models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = False
        db_table = 'muestra_secuencia'


class RegistroUso(models.Model):
    id_registro = models.AutoField(primary_key=True)
    sena = models.ForeignKey(Sena, on_delete=models.DO_NOTHING, db_column='id_sena',
                             null=True, blank=True, related_name='registros_uso')
    nombre_sena = models.CharField(max_length=100)
    fecha_hora = models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = False
        db_table = 'registro_traduccion'
        ordering = ['-fecha_hora']

    def __str__(self):
        return f"{self.nombre_sena} - {self.fecha_hora:%Y-%m-%d %H:%M}"


class Usuario(models.Model):
    id_usuario = models.AutoField(primary_key=True)
    nombre = models.CharField(max_length=100)
    email = models.CharField(max_length=150)
    password_hash = models.CharField(max_length=255)
    rol = models.CharField(max_length=30)
    foto = models.TextField(blank=True, null=True)
    activo = models.BooleanField(default=True)

    class Meta:
        managed = False
        db_table = 'usuario'
        ordering = ['-id_usuario']

    def __str__(self):
        return self.nombre