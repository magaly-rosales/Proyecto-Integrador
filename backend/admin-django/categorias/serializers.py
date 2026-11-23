import re

from rest_framework import serializers

from senas.models import Categoria, Sena


class CategoriaSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(source='id_categoria', read_only=True)
    imagen = serializers.SerializerMethodField()   # solo lectura: se sube aparte (multipart)
    total_senas = serializers.SerializerMethodField()

    class Meta:
        model = Categoria
        fields = ['id', 'nombre', 'color', 'imagen', 'total_senas']

    def get_imagen(self, obj):
        if not obj.imagen:
            return None
        request = self.context.get('request')
        return request.build_absolute_uri(obj.imagen) if request else obj.imagen

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


class SenaDeCategoriaSerializer(serializers.ModelSerializer):
    """Seña resumida, para la pantalla 'señas de una categoría'."""
    id = serializers.IntegerField(source='id_sena', read_only=True)

    class Meta:
        model = Sena
        fields = ['id', 'nombre', 'descripcion', 'imagen']