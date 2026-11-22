from rest_framework import serializers
from django.contrib.auth.models import User
from .models import Sena, RegistroUso, Usuario


class SenaSerializer(serializers.ModelSerializer):
    # Se mantienen los nombres que ya usa el frontend de Jeff
    id = serializers.IntegerField(source='id_sena', read_only=True)
    fecha_creacion = serializers.DateTimeField(source='fecha_registro', read_only=True)
    video = serializers.SerializerMethodField()

    class Meta:
        model = Sena
        fields = ['id', 'nombre', 'categoria', 'descripcion', 'video', 'fecha_creacion']

    def get_video(self, obj):
        v = obj.videos.order_by('-fecha_subida').first()
        if not v:
            return None
        request = self.context.get('request')
        return request.build_absolute_uri(v.url_video) if request else v.url_video

    def validate_nombre(self, value):
        if not value or not value.strip():
            raise serializers.ValidationError("CA-04: El Nombre de la seña no puede estar vacío.")
        return value.strip()

    def validate_categoria(self, value):
        if not value or not value.strip():
            raise serializers.ValidationError("CA-04: La Categoría no puede estar vacía.")
        return value.strip()


class UserSerializer(serializers.ModelSerializer):
    # Nombres que ya usa el frontend, leídos de la tabla usuario
    id = serializers.IntegerField(source='id_usuario', read_only=True)
    username = serializers.CharField(source='nombre', read_only=True)
    is_active = serializers.BooleanField(source='activo')
    is_staff = serializers.SerializerMethodField()

    class Meta:
        model = Usuario
        fields = ['id', 'username', 'email', 'is_active', 'is_staff']
        read_only_fields = ['email']

    def get_is_staff(self, obj):
        return obj.rol.upper() in ('ADMIN', 'ADMINISTRADOR')
class RegistroUsoSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(source='id_registro', read_only=True)
    nombre_sena_consultada = serializers.CharField(source='nombre_sena', max_length=100)
    fecha_consulta = serializers.DateTimeField(source='fecha_hora', read_only=True)

    class Meta:
        model = RegistroUso
        fields = ['id', 'sena', 'nombre_sena_consultada', 'fecha_consulta']
        
        
        
        
        
        
        
        
        