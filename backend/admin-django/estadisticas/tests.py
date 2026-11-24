"""Pruebas de la app estadisticas (resumen del menú y estadísticas de uso)."""
from datetime import datetime, timedelta

from django.test import SimpleTestCase
from django.utils import timezone
from rest_framework.test import APIClient, APITestCase

from config.pruebas_comunes import cliente_de, crear_admin, crear_categoria, crear_sena, crear_usuario
from senas.models import RegistroUso, Sena

from .services import rango_fechas


# ======================= PRUEBAS UNITARIAS =======================

class RangoFechasUnitTests(SimpleTestCase):
    def test_por_defecto_son_los_ultimos_30_dias(self):
        inicio, fin = rango_fechas()
        esperado = timezone.now() - timedelta(days=30)
        self.assertLess(abs((inicio - esperado).total_seconds()), 5)
        self.assertIsNone(fin)

    def test_ultimos_7_dias(self):
        inicio, _ = rango_fechas('7')
        self.assertLess(abs((inicio - (timezone.now() - timedelta(days=7))).total_seconds()), 5)

    def test_todo_no_tiene_limites(self):
        self.assertEqual(rango_fechas('todo'), (None, None))
        self.assertEqual(rango_fechas('TODO'), (None, None))

    def test_dias_invalido_lanza_error(self):
        with self.assertRaises(ValueError):
            rango_fechas('5')

    def test_rango_propio_cubre_el_dia_completo(self):
        inicio, fin = rango_fechas(desde='2026-11-01', hasta='2026-11-30')
        self.assertEqual(inicio, datetime(2026, 11, 1, 0, 0, 0))
        self.assertEqual(fin, datetime(2026, 11, 30, 23, 59, 59, 999999))

    def test_solo_desde_deja_el_final_abierto(self):
        inicio, fin = rango_fechas(desde='2026-11-01')
        self.assertEqual(inicio, datetime(2026, 11, 1))
        self.assertIsNone(fin)

    def test_el_rango_propio_tiene_prioridad_sobre_dias(self):
        inicio, _ = rango_fechas(dias='7', desde='2026-01-01')
        self.assertEqual(inicio, datetime(2026, 1, 1))

    def test_desde_posterior_a_hasta_lanza_error(self):
        with self.assertRaises(ValueError):
            rango_fechas(desde='2026-12-01', hasta='2026-11-01')

    def test_formato_de_fecha_invalido_lanza_error(self):
        with self.assertRaises(ValueError):
            rango_fechas(desde='01/11/2026')


# ====================== PRUEBAS DE INTEGRACIÓN ======================

class ResumenApiTests(APITestCase):
    """GET /api/resumen/ — números de las tarjetas y del menú lateral."""

    def setUp(self):
        self.cliente = cliente_de(crear_admin())

    def test_sin_sesion_de_administrador_da_403(self):
        self.assertEqual(APIClient().get('/api/resumen/').status_code, 403)

    def test_sin_datos_todo_es_cero(self):
        r = self.cliente.get('/api/resumen/').json()
        self.assertEqual(r['senas'], {'total': 0, 'nuevas_semana': 0})
        self.assertEqual(r['categorias'], {'total': 0})
        self.assertEqual(r['usuarios'], {'total': 0, 'activos': 0, 'desactivados': 0})

    def test_cuenta_señas_categorias_y_usuarios(self):
        cat = crear_categoria('Saludos')
        crear_categoria('Familia')
        vieja = crear_sena('Vieja', cat)
        crear_sena('Nueva', cat)
        Sena.objects.filter(pk=vieja.pk).update(fecha_registro=timezone.now() - timedelta(days=30))
        crear_usuario('Ana', 'ana@correo.com')
        crear_usuario('Luis', 'luis@correo.com', activo=False)
        r = self.cliente.get('/api/resumen/').json()
        self.assertEqual(r['senas'], {'total': 2, 'nuevas_semana': 1})
        self.assertEqual(r['categorias']['total'], 2)
        # el administrador no cuenta como usuario de la app
        self.assertEqual(r['usuarios'], {'total': 2, 'activos': 1, 'desactivados': 1})


class EstadisticasApiTests(APITestCase):
    """GET /api/estadisticas/"""

    def setUp(self):
        self.cliente = cliente_de(crear_admin())

    def _traducir(self, nombre, veces, hace_dias=0):
        for _ in range(veces):
            r = RegistroUso.objects.create(nombre_sena=nombre)
            if hace_dias:
                RegistroUso.objects.filter(pk=r.pk).update(
                    fecha_hora=timezone.now() - timedelta(days=hace_dias))

    def test_sin_sesion_de_administrador_da_403(self):
        self.assertEqual(APIClient().get('/api/estadisticas/').status_code, 403)

    def test_sin_traducciones(self):
        r = self.cliente.get('/api/estadisticas/', {'dias': 'todo'}).json()
        self.assertEqual((r['total_traducciones'], r['top'], r['mas_traducida']), (0, [], None))

    def test_ranking_ordenado_con_porcentajes(self):
        self._traducir('Hola', 3)
        self._traducir('Gracias', 1)
        r = self.cliente.get('/api/estadisticas/', {'dias': 'todo'}).json()
        self.assertEqual(r['total_traducciones'], 4)
        self.assertEqual([(t['nombre'], t['total'], t['porcentaje']) for t in r['top']],
                         [('Hola', 3, 75.0), ('Gracias', 1, 25.0)])
        self.assertEqual(r['mas_traducida'], {'nombre': 'Hola', 'total': 3, 'siguiente': 'Gracias'})

    def test_el_filtro_de_7_dias_deja_fuera_lo_antiguo(self):
        self._traducir('Hola', 2)
        self._traducir('Antigua', 5, hace_dias=20)
        r = self.cliente.get('/api/estadisticas/', {'dias': '7'}).json()
        self.assertEqual([t['nombre'] for t in r['top']], ['Hola'])
        todo = self.cliente.get('/api/estadisticas/', {'dias': 'todo'}).json()
        self.assertEqual(todo['total_traducciones'], 7)

    def test_rango_propio_desde_y_hasta(self):
        self._traducir('Hola', 2)
        hoy = timezone.now().date().isoformat()
        r = self.cliente.get('/api/estadisticas/', {'desde': hoy, 'hasta': hoy}).json()
        self.assertEqual(r['total_traducciones'], 2)
        self.assertEqual(r['rango'], {'desde': hoy, 'hasta': hoy})

    def test_dias_invalido_da_400(self):
        r = self.cliente.get('/api/estadisticas/', {'dias': '5'})
        self.assertEqual(r.status_code, 400)
        self.assertFalse(r.json()['exito'])

    def test_rango_invertido_da_400(self):
        r = self.cliente.get('/api/estadisticas/', {'desde': '2026-12-01', 'hasta': '2026-11-01'})
        self.assertEqual(r.status_code, 400)

    def test_incluye_el_resumen_de_usuarios(self):
        crear_usuario('Ana', 'ana@correo.com')
        crear_usuario('Luis', 'luis@correo.com', activo=False)
        usuarios = self.cliente.get('/api/estadisticas/', {'dias': 'todo'}).json()['usuarios']
        self.assertEqual(usuarios, {'total': 2, 'activos': 1, 'inactivos': 1,
                                     'porcentaje_activos': 50.0, 'porcentaje_inactivos': 50.0})
