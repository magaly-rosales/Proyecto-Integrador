"""Cálculos de las estadísticas del panel (separados de las vistas)."""
from datetime import datetime, time, timedelta

from django.db.models import Count
from django.utils import timezone

from senas.models import Categoria, RegistroUso, Sena
from usuarios.services import usuarios_de_la_app


def _fecha(texto):
    """'2026-11-22' -> date. Lanza ValueError si el formato no es válido."""
    return datetime.strptime(texto, '%Y-%m-%d').date()


def rango_fechas(dias=None, desde=None, hasta=None):
    """Devuelve (desde, hasta) como datetime; None significa sin límite.

    Prioridad: desde/hasta (rango propio) > dias ('7', '30', 'todo'). Por defecto 30 días.
    """
    if desde or hasta:
        inicio = datetime.combine(_fecha(desde), time.min) if desde else None
        fin = datetime.combine(_fecha(hasta), time.max) if hasta else None
        if inicio and fin and inicio > fin:
            raise ValueError('La fecha "desde" no puede ser posterior a "hasta".')
        return inicio, fin
    dias = (dias or '30').lower()
    if dias == 'todo':
        return None, None
    if dias not in ('7', '30'):
        raise ValueError('El parámetro dias debe ser 7, 30 o todo.')
    return timezone.now() - timedelta(days=int(dias)), None


def traducciones(inicio, fin):
    qs = RegistroUso.objects.all()
    if inicio:
        qs = qs.filter(fecha_hora__gte=inicio)
    if fin:
        qs = qs.filter(fecha_hora__lte=fin)
    return qs


def top_senas(qs, limite=10):
    """Traducciones por seña, con su porcentaje del total."""
    total = qs.count()
    filas = (
        qs.values('nombre_sena')
        .annotate(total=Count('id_registro'))
        .order_by('-total', 'nombre_sena')[:limite]
    )
    return [
        {
            'nombre': f['nombre_sena'],
            'total': f['total'],
            'porcentaje': round(f['total'] * 100 / total, 1) if total else 0,
        }
        for f in filas
    ]


def resumen_usuarios():
    usuarios = usuarios_de_la_app()
    total = usuarios.count()
    activos = usuarios.filter(activo=True).count()
    return {
        'total': total,
        'activos': activos,
        'inactivos': total - activos,
        'porcentaje_activos': round(activos * 100 / total, 1) if total else 0,
    }


def resumen_general():
    """Números de las tarjetas y del menú lateral."""
    usuarios = resumen_usuarios()
    return {
        'senas': {
            'total': Sena.objects.count(),
            'nuevas_semana': Sena.objects.filter(
                fecha_registro__gte=timezone.now() - timedelta(days=7)).count(),
        },
        'categorias': {'total': Categoria.objects.count()},
        'usuarios': {
            'total': usuarios['total'],
            'activos': usuarios['activos'],
            'desactivados': usuarios['inactivos'],
        },
    }