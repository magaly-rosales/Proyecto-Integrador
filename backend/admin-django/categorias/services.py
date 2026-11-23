"""Reglas de negocio de las categorías (separadas de las vistas)."""
from django.db import transaction

from senas.archivos import borrar_archivo
from senas.models import RegistroUso, Sena, VideoReferencia


def guardar_categoria(serializer, ruta_imagen=None):
    """Guarda la categoría y deja igual el texto antiguo de sus señas.

    Spring Boot (web de usuario) todavía lee sena.categoria como texto,
    por eso se actualiza junto con el nombre. Si llega una imagen nueva,
    se guarda su ruta en la categoría.
    """
    with transaction.atomic():
        extra = {'imagen': ruta_imagen} if ruta_imagen else {}
        categoria = serializer.save(**extra)
        Sena.objects.filter(categoria_ref=categoria).update(categoria=categoria.nombre)
    return categoria


def eliminar_categoria(categoria):
    """Elimina la categoría y todas sus señas (con videos y muestras de entrenamiento).

    El historial de traducciones se conserva: solo se desvincula de la seña
    (queda el nombre guardado en el registro). También se borran del disco las
    imágenes y videos. Devuelve cuántas señas se eliminaron.
    """
    with transaction.atomic():
        senas = Sena.objects.filter(categoria_ref=categoria)
        total = senas.count()
        archivos = list(senas.values_list('imagen', flat=True))
        archivos += list(VideoReferencia.objects.filter(sena__in=senas).values_list('url_video', flat=True))
        archivos.append(categoria.imagen)
        RegistroUso.objects.filter(sena__in=senas).update(sena=None)
        senas.delete()
        categoria.delete()

    # Ya eliminado de la base: si un archivo no se puede borrar, no se vuelve a fallar
    for ruta in archivos:
        if ruta:
            try:
                borrar_archivo(ruta)
            except OSError:
                pass
    return total