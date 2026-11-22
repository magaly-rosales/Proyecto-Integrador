from rest_framework import serializers

from senas.auth import es_admin
from senas.models import Usuario


class UsuarioSerializer(serializers.ModelSerializer):
    # Nombres que ya usa el frontend, leídos de la tabla usuario
    id = serializers.IntegerField(source='id_usuario', read_only=True)
    username = serializers.CharField(source='nombre', read_only=True)
    is_active = serializers.BooleanField(source='activo')
    is_staff = serializers.SerializerMethodField()
    iniciales = serializers.SerializerMethodField()

    class Meta:
        model = Usuario
        fields = ['id', 'nombre', 'username', 'email', 'rol', 'is_active', 'is_staff', 'iniciales']
        read_only_fields = ['nombre', 'email', 'rol']

    def get_is_staff(self, obj):
        return es_admin(obj)

    def get_iniciales(self, obj):
        palabras = obj.nombre.split()
        return ''.join(p[0] for p in palabras[:2]).upper()