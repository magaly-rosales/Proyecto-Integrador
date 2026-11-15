from django.contrib import admin
from django.db.models import Count

from .models import (
    MuestraSecuencia,
    MuestraSena,
    RegistroUso,
    Sena,
    Usuario,
    VideoReferencia,
)

admin.site.site_header = 'SeñasApp – Administración'
admin.site.site_title = 'SeñasApp Admin'
admin.site.index_title = 'Panel de administración'


# ---------------------------------------------------------------------------
# Seña (modelo principal): se relaciona con videos, muestras y movimientos
# ---------------------------------------------------------------------------
class VideoReferenciaInline(admin.TabularInline):
    """Los videos de referencia se editan dentro de la misma seña."""
    model = VideoReferencia
    extra = 0
    fields = ('url_video', 'formato', 'tamano_mb', 'fecha_subida')
    readonly_fields = ('fecha_subida',)


@admin.register(Sena)
class SenaAdmin(admin.ModelAdmin):
    list_display = (
        'id_sena', 'nombre', 'categoria',
        'total_videos', 'total_muestras', 'total_movimientos', 'fecha_registro',
    )
    list_filter = ('categoria',)
    search_fields = ('nombre', 'categoria', 'descripcion')
    ordering = ('-fecha_registro',)
    list_per_page = 25
    readonly_fields = ('fecha_registro', 'total_muestras', 'total_movimientos')
    inlines = [VideoReferenciaInline]

    def get_queryset(self, request):
        # Una sola consulta con los conteos de las tablas relacionadas
        return super().get_queryset(request).annotate(
            _videos=Count('videos', distinct=True),
            _muestras=Count('muestras', distinct=True),
            _movimientos=Count('secuencias', distinct=True),
        )

    @admin.display(description='Videos', ordering='_videos')
    def total_videos(self, obj):
        return getattr(obj, '_videos', 0)

    @admin.display(description='Muestras (señas quietas)', ordering='_muestras')
    def total_muestras(self, obj):
        return getattr(obj, '_muestras', 0)

    @admin.display(description='Movimientos grabados', ordering='_movimientos')
    def total_movimientos(self, obj):
        return getattr(obj, '_movimientos', 0)


# ---------------------------------------------------------------------------
# Video de referencia
# ---------------------------------------------------------------------------
@admin.register(VideoReferencia)
class VideoReferenciaAdmin(admin.ModelAdmin):
    list_display = ('id_video', 'sena', 'url_video', 'formato', 'tamano_mb', 'fecha_subida')
    list_filter = ('formato', 'sena')
    search_fields = ('sena__nombre', 'url_video')
    list_select_related = ('sena',)
    readonly_fields = ('fecha_subida',)


# ---------------------------------------------------------------------------
# Muestras de entrenamiento (las graba la cámara del admin)
# ---------------------------------------------------------------------------
@admin.register(MuestraSena)
class MuestraSenaAdmin(admin.ModelAdmin):
    list_display = ('id_muestra', 'sena', 'puntos_resumen', 'fecha_registro')
    list_filter = ('sena', 'fecha_registro')
    search_fields = ('sena__nombre',)
    list_select_related = ('sena',)
    list_per_page = 25
    readonly_fields = ('fecha_registro',)

    @admin.display(description='Puntos (resumen)')
    def puntos_resumen(self, obj):
        texto = str(obj.puntos or '')
        return texto[:50] + '…' if len(texto) > 50 else texto


@admin.register(MuestraSecuencia)
class MuestraSecuenciaAdmin(admin.ModelAdmin):
    list_display = ('id_secuencia', 'sena', 'num_cuadros', 'fecha_registro')
    list_filter = ('sena', 'fecha_registro')
    search_fields = ('sena__nombre',)
    list_select_related = ('sena',)
    fields = ('sena', 'num_cuadros', 'cuadros_resumen', 'fecha_registro')
    readonly_fields = ('cuadros_resumen', 'fecha_registro')

    @admin.display(description='Cuadros (resumen)')
    def cuadros_resumen(self, obj):
        texto = str(obj.cuadros or '')
        return texto[:80] + '…' if len(texto) > 80 else texto

    def has_add_permission(self, request):
        # Los movimientos se graban con la cámara, no a mano
        return False


# ---------------------------------------------------------------------------
# Registro de traducciones (solo lectura: es un historial)
# ---------------------------------------------------------------------------
@admin.register(RegistroUso)
class RegistroUsoAdmin(admin.ModelAdmin):
    list_display = ('id_registro', 'nombre_sena', 'sena', 'fecha_hora')
    list_filter = ('fecha_hora',)
    search_fields = ('nombre_sena',)
    list_select_related = ('sena',)
    date_hierarchy = 'fecha_hora'
    list_per_page = 25

    def has_add_permission(self, request):
        return False

    def has_change_permission(self, request, obj=None):
        return False


# ---------------------------------------------------------------------------
# Usuarios de la app (se registran desde la web; el admin solo los gestiona)
# ---------------------------------------------------------------------------
@admin.register(Usuario)
class UsuarioAdmin(admin.ModelAdmin):
    list_display = ('id_usuario', 'nombre', 'email', 'rol', 'activo')
    list_filter = ('rol', 'activo')
    search_fields = ('nombre', 'email')
    exclude = ('password_hash', 'foto') 
    actions = ['activar_usuarios', 'desactivar_usuarios']

    def has_add_permission(self, request):
        return False

    @admin.action(description='Activar usuarios seleccionados')
    def activar_usuarios(self, request, queryset):
        cantidad = queryset.update(activo=True)
        self.message_user(request, f'{cantidad} usuario(s) activado(s).')

    @admin.action(description='Desactivar usuarios seleccionados')
    def desactivar_usuarios(self, request, queryset):
        cantidad = queryset.update(activo=False)
        self.message_user(request, f'{cantidad} usuario(s) desactivado(s).')