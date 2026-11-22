import re

from rest_framework import serializers

from senas.models import Categoria, Sena

# Íconos que el panel sabe dibujar (se pueden ir agregando)
ICONOS = (
    'hand', 'heart', 'star', 'zap', 'message', 'map-pin', 'users', 'hash',
    'alert', 'home', 'book', 'clock', 'smile', 'utensils', 'bus', 'briefcase',
)


class CategoriaSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(source='id_categoria', read_only=True)
    total_senas = serializers.SerializerMethodField()

    class Meta:
        model = Categoria
        fields = ['id', 'nombre', 'color', 'icono', 'total_senas']

    def get_total_senas(self, obj):
        total = getattr(obj, '_total', None)
        return total if total is not None else obj.senas.count()

    def validate_nombre(self, value):
        value = (value or '').strip()
        if not value:
            raise serializers.ValidationError('El nombre de la categoría no puede estar vacío.')
        repetidas = Categoria.objects.filter(nombre__iexact=value)
        if self.instance:
            repetidas = repetidas.exclude(pk=self.instance.pk)
        if repetidas.exists():
            raise serializers.ValidationError('Ya existe una categoría con ese nombre.')
        return value

    def validate_color(self, value):
        if not re.fullmatch(r'#[0-9A-Fa-f]{6}', value or ''):
            raise serializers.ValidationError('El color debe tener el formato #RRGGBB.')
        return value.upper()

    def validate_icono(self, value):
        if value not in ICONOS:
            raise serializers.ValidationError('Ícono no válido.')
        return value


class SenaDeCategoriaSerializer(serializers.ModelSerializer):
    """Seña resumida, para la pantalla 'señas de una categoría'."""
    id = serializers.IntegerField(source='id_sena', read_only=True)

    class Meta:
        model = Sena
        fields = ['id', 'nombre', 'descripcion', 'imagen']