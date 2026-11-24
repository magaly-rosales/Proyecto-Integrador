from datetime import timedelta

from django.utils import timezone
from rest_framework import serializers

from .models import Categoria, RegistroUso, Sena
from .progreso import META_MOVIMIENTOS, META_MUESTRAS, esta_entrenada, faltan


class SenaSerializer(serializers.ModelSerializer):
    # Se mantienen los nombres que ya usa el frontend
    id = serializers.IntegerField(source='id_sena', read_only=True)
    fecha_creacion = serializers.DateTimeField(source='fecha_registro', read_only=True)

    # Categoría: se escribe con categoria_id y se lee como texto + color + ícono
    categoria = serializers.CharField(read_only=True)
    categoria_id = serializers.PrimaryKeyRelatedField(
        source='categoria_ref', queryset=Categoria.objects.all(), required=False)
    categoria_color = serializers.SerializerMethodField()
    categoria_icono = serializers.SerializerMethodField()

    # Archivos y estado (solo lectura)
    imagen = serializers.SerializerMethodField()
    video = serializers.SerializerMethodField()
    muestras = serializers.SerializerMethodField()
    movimientos = serializers.SerializerMethodField()
    entrenada = serializers.SerializerMethodField()
    faltan = serializers.SerializerMethodField()
    es_nueva = serializers.SerializerMethodField()

    class Meta:
        model = Sena
        fields = [
            'id', 'nombre', 'descripcion',
            'categoria', 'categoria_id', 'categoria_color', 'categoria_icono',
            'imagen', 'video',
            'muestras', 'movimientos', 'entrenada', 'faltan', 'es_nueva',
            'fecha_creacion',
        ]

    # ---------- lectura ----------
    def _url(self, ruta):
        if not ruta:
            return None
        request = self.context.get('request')
        return request.build_absolute_uri(ruta) if request else ruta

    def get_imagen(self, obj):
        return self._url(obj.imagen)

    def get_video(self, obj):
        v = max(obj.videos.all(), key=lambda x: x.fecha_subida, default=None)
        return self._url(v.url_video) if v else None

    def get_categoria_color(self, obj):
        return obj.categoria_ref.color if obj.categoria_ref else None

    def get_categoria_icono(self, obj):
        return obj.categoria_ref.icono if obj.categoria_ref else None

    def _n_muestras(self, obj):
        n = getattr(obj, '_muestras', None)
        return n if n is not None else obj.muestras.count()

    def _n_movimientos(self, obj):
        n = getattr(obj, '_secuencias', None)
        return n if n is not None else obj.secuencias.count()

    def get_muestras(self, obj):
        return {'actual': self._n_muestras(obj), 'meta': META_MUESTRAS}

    def get_movimientos(self, obj):
        return {'actual': self._n_movimientos(obj), 'meta': META_MOVIMIENTOS}

    def get_entrenada(self, obj):
        return esta_entrenada(self._n_muestras(obj), self._n_movimientos(obj))

    def get_faltan(self, obj):
        return faltan(self._n_muestras(obj), self._n_movimientos(obj))

    def get_es_nueva(self, obj):
        return obj.fecha_registro >= timezone.now() - timedelta(days=7)

    # ---------- escritura ----------
    def validate_nombre(self, value):
        if not value or not value.strip():
            raise serializers.ValidationError("CA-04: El Nombre de la seña no puede estar vacío.")
        value = value.strip()
        repetidas = Sena.objects.filter(nombre__iexact=value)
        if self.instance:
            repetidas = repetidas.exclude(pk=self.instance.pk)
        if repetidas.exists():
            raise serializers.ValidationError("Ya existe una seña con ese nombre.")
        return value

    def validate(self, attrs):
        categoria = attrs.get('categoria_ref')
        if categoria is None and 'categoria_ref' not in attrs:
            # Compatibilidad: el panel antiguo enviaba el nombre de la categoría como texto
            texto = (self.initial_data.get('categoria') or '').strip()
            if texto:
                categoria = Categoria.objects.filter(nombre__iexact=texto).first()
                if not categoria:
                    raise serializers.ValidationError({'categoria_id': 'La categoría no existe.'})
                attrs['categoria_ref'] = categoria
            elif not self.instance:
                raise serializers.ValidationError({'categoria_id': 'CA-04: Elige una categoría.'})
        if categoria is not None:
            # Spring Boot todavía lee el texto antiguo sena.categoria
            attrs['categoria'] = categoria.nombre
        return attrs


class RegistroUsoSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(source='id_registro', read_only=True)
    nombre_sena_consultada = serializers.CharField(source='nombre_sena', max_length=100)
    fecha_consulta = serializers.DateTimeField(source='fecha_hora', read_only=True)

    class Meta:
        model = RegistroUso
        fields = ['id', 'sena', 'nombre_sena_consultada', 'fecha_consulta']