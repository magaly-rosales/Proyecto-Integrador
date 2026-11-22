from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response

from . import services


@api_view(['GET'])
def estadisticas_uso(request):
    """GET /api/estadisticas/?dias=7|30|todo  o  ?desde=AAAA-MM-DD&hasta=AAAA-MM-DD"""
    p = request.query_params
    try:
        inicio, fin = services.rango_fechas(p.get('dias'), p.get('desde'), p.get('hasta'))
    except ValueError as e:
        return Response({"exito": False, "error": f"Rango no válido: {e}"},
                        status=status.HTTP_400_BAD_REQUEST)

    registros = services.traducciones(inicio, fin)
    top = services.top_senas(registros, limite=10)
    return Response({
        "exito": True,
        "rango": {
            "desde": inicio.date().isoformat() if inicio else None,
            "hasta": fin.date().isoformat() if fin else None,
        },
        "total_traducciones": registros.count(),
        "mas_traducida": {
            "nombre": top[0]['nombre'],
            "total": top[0]['total'],
            "siguiente": top[1]['nombre'] if len(top) > 1 else None,
        } if top else None,
        "top": top,
        # Formato antiguo, por si algún componente todavía lo usa
        "top_senas": [{"nombre_sena_consultada": t['nombre'], "total": t['total']} for t in top[:5]],
        "usuarios": services.resumen_usuarios(),
    })


@api_view(['GET'])
def resumen(request):
    """GET /api/resumen/ -> números de las tarjetas y del menú lateral."""
    return Response({"exito": True, **services.resumen_general()})