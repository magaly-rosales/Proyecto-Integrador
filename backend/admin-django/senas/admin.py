from django.contrib import admin
from .models import Sena

@admin.register(Sena)
class SenaAdmin(admin.ModelAdmin):
    list_display = ('id', 'nombre', 'categoria', 'fecha_creacion')
    list_filter = ('categoria', 'fecha_creacion')
    search_fields = ('nombre', 'categoria')
    readonly_fields = ('fecha_creacion',)