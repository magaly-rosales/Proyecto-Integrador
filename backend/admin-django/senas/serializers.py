from rest_framework import serializers
from django.contrib.auth.models import User
from .models import Sena

class SenaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Sena
        fields = ['id', 'nombre', 'categoria', 'descripcion', 'video', 'fecha_creacion']
        read_only_fields = ['id', 'fecha_creacion']

    def validate_nombre(self, value):
        if not value or not value.strip():
            raise serializers.ValidationError("CA-04: El Nombre de la seña no puede estar vacío.")
        return value.strip()

    def validate_categoria(self, value):
        if not value or not value.strip():
            raise serializers.ValidationError("CA-04: La Categoría no puede estar vacía.")
        return value.strip()


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'first_name', 'last_name', 'is_active', 'is_staff', 'date_joined']
        read_only_fields = ['id', 'date_joined']