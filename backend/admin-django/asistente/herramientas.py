"""Herramientas que la IA puede usar para consultar el panel.

Todas leen datos reales de la base de datos y son de solo lectura, salvo
'cambiar_estado_usuario', que NO cambia nada: solo deja la acción propuesta
para que el administrador la confirme con un botón.
"""
from django.db.models import Count

from estadisticas.services import rango_fechas, resumen_general, resumen_usuarios, top_senas, traducciones
from senas.models import Categoria, Sena
from senas.progreso import esta_entrenada
from usuarios.services import filtrar, usuarios_de_la_app

LIMITE = 15


class Contexto:
    """Guarda la acción pendiente de confirmación que propone la IA (si propone alguna)."""

    def __init__(self):
        self.accion = None


def _senas_con_conteos():
    return Sena.objects.select_related('categoria_ref').annotate(
        n_muestras=Count('muestras', distinct=True),
        n_secuencias=Count('secuencias', distinct=True),
    )


def resumen_panel(contexto):
    general = resumen_general()
    return {
        'senas_total': general['senas']['total'],
        'senas_nuevas_ultima_semana': general['senas']['nuevas_semana'],
        'categorias_total': general['categorias']['total'],
        'usuarios': resumen_usuarios(),
    }


def buscar_usuarios(contexto, texto='', estado=''):
    coincidencias = filtrar(usuarios_de_la_app(), texto=texto or None, estado=estado or None)
    return {
        'total_coincidencias': coincidencias.count(),
        'usuarios': [
            {'id': u.id_usuario, 'nombre': u.nombre, 'email': u.email, 'activo': u.activo}
            for u in coincidencias[:LIMITE]
        ],
    }


def listar_categorias(contexto):
    categorias = Categoria.objects.annotate(total_senas=Count('senas')).order_by('nombre')
    return {
        'total': categorias.count(),
        'categorias': [{'nombre': c.nombre, 'total_senas': c.total_senas} for c in categorias],
    }


def buscar_senas(contexto, texto='', categoria=''):
    senas = _senas_con_conteos()
    if texto:
        senas = senas.filter(nombre__icontains=texto)
    if categoria:
        senas = senas.filter(categoria_ref__nombre__icontains=categoria)
    return {
        'total_coincidencias': senas.count(),
        'senas': [
            {
                'nombre': s.nombre,
                'categoria': s.categoria_ref.nombre if s.categoria_ref else s.categoria,
                'entrenada': esta_entrenada(s.n_muestras, s.n_secuencias),
                'muestras': s.n_muestras,
                'movimientos': s.n_secuencias,
            }
            for s in senas.order_by('nombre')[:LIMITE]
        ],
    }


def estado_entrenamiento(contexto):
    senas = list(_senas_con_conteos().order_by('nombre'))
    pendientes = [s.nombre for s in senas if not esta_entrenada(s.n_muestras, s.n_secuencias)]
    return {
        'total_senas': len(senas),
        'entrenadas': len(senas) - len(pendientes),
        'por_entrenar': len(pendientes),
        'nombres_por_entrenar': pendientes[:LIMITE],
    }


def top_traducciones(contexto, dias='30'):
    dias = str(dias).lower()
    if dias not in ('7', '30', 'todo'):
        dias = '30'
    inicio, fin = rango_fechas(dias)
    registros = traducciones(inicio, fin)
    return {
        'periodo': 'todo el tiempo' if dias == 'todo' else f'últimos {dias} días',
        'total_traducciones': registros.count(),
        'mas_traducidas': top_senas(registros, limite=5),
    }


def cambiar_estado_usuario(contexto, id_usuario, activar):
    """No cambia nada: propone la acción y el administrador la confirma en el panel."""
    usuario = usuarios_de_la_app().filter(pk=int(id_usuario)).first()
    if not usuario:
        return {'error': 'No existe un usuario con ese id. Búscalo primero con buscar_usuarios.'}
    activar = bool(activar)
    if usuario.activo == activar:
        return {'sin_cambios': True, 'mensaje': f"{usuario.nombre} ya está {'activo' if activar else 'inactivo'}."}
    contexto.accion = {
        'tipo': 'cambiar_estado',
        'id_usuario': usuario.id_usuario,
        'nombre': usuario.nombre,
        'activar': activar,
    }
    return {
        'pendiente_de_confirmacion': True,
        'usuario': usuario.nombre,
        'accion': 'activar' if activar else 'desactivar',
        'nota': 'El cambio aún no se hizo: el administrador debe presionar Confirmar en el panel.',
    }


FUNCIONES = {
    'resumen_panel': resumen_panel,
    'buscar_usuarios': buscar_usuarios,
    'listar_categorias': listar_categorias,
    'buscar_senas': buscar_senas,
    'estado_entrenamiento': estado_entrenamiento,
    'top_traducciones': top_traducciones,
    'cambiar_estado_usuario': cambiar_estado_usuario,
}


def ejecutar(nombre, argumentos, contexto):
    """Corre la herramienta que pidió la IA; un nombre o argumentos inválidos devuelven un error, no una excepción."""
    funcion = FUNCIONES.get(nombre)
    if funcion is None:
        return {'error': f'La herramienta {nombre} no existe.'}
    try:
        return funcion(contexto, **(argumentos or {}))
    except (TypeError, ValueError):
        return {'error': 'Los parámetros enviados no son válidos.'}


def _funcion(nombre, descripcion, propiedades=None, obligatorios=None):
    declaracion = {'name': nombre, 'description': descripcion}
    if propiedades:
        declaracion['parameters'] = {
            'type': 'object',
            'properties': propiedades,
            'required': obligatorios or [],
        }
    return declaracion


DECLARACIONES = [
    _funcion('resumen_panel',
             'Totales del panel: señas, categorías y usuarios (total, activos, inactivos y sus porcentajes).'),
    _funcion('buscar_usuarios',
             'Busca usuarios de la web y la app por nombre o correo y dice si están activos. '
             'Sin parámetros lista los primeros usuarios.',
             {'texto': {'type': 'string', 'description': 'Parte del nombre o del correo.'},
              'estado': {'type': 'string', 'description': "Opcional: 'activo' o 'inactivo'."}}),
    _funcion('listar_categorias', 'Lista todas las categorías con la cantidad de señas de cada una.'),
    _funcion('buscar_senas',
             'Busca señas por nombre o categoría e indica si están entrenadas y cuántos ejemplos tienen.',
             {'texto': {'type': 'string', 'description': 'Parte del nombre de la seña.'},
              'categoria': {'type': 'string', 'description': 'Parte del nombre de la categoría.'}}),
    _funcion('estado_entrenamiento', 'Cuántas señas están entrenadas y cuáles faltan por entrenar.'),
    _funcion('top_traducciones',
             'Las señas más traducidas por los usuarios y el total de traducciones en un período.',
             {'dias': {'type': 'string', 'description': "'7', '30' o 'todo'. Por defecto '30'."}}),
    _funcion('cambiar_estado_usuario',
             'Propone activar o desactivar la cuenta de un usuario. No lo hace directamente: '
             'el administrador debe confirmarlo. Usa antes buscar_usuarios para obtener el id.',
             {'id_usuario': {'type': 'integer', 'description': 'Id del usuario.'},
              'activar': {'type': 'boolean', 'description': 'true para activar, false para desactivar.'}},
             ['id_usuario', 'activar']),
]
