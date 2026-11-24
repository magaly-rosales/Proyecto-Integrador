"""Pruebas de la app categorias.

Unitarias:   validaciones sin base de datos (SimpleTestCase).
Integración: la API completa con una base de datos de pruebas (APITestCase).
"""
import os

from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import SimpleTestCase
from rest_framework.exceptions import ValidationError as ErrorDRF
from rest_framework.test import APITestCase

from config.pruebas_comunes import (
    CarpetaMediaTemporal, cliente_de, crear_admin, crear_categoria, crear_sena, imagen_png, ruta_en_disco,
)
from senas.archivos import guardar_imagen, guardar_video
from senas.models import Categoria, MuestraSena, RegistroUso, Sena, VideoReferencia

from .serializers import CategoriaSerializer


# ======================= PRUEBAS UNITARIAS =======================

class ColorUnitTests(SimpleTestCase):
    def setUp(self):
        self.serializer = CategoriaSerializer()

    def test_acepta_un_color_hexadecimal_y_lo_pasa_a_mayusculas(self):
        self.assertEqual(self.serializer.validate_color('#2bbcc9'), '#2BBCC9')

    def test_rechaza_colores_con_formato_invalido(self):
        for color in ('2BBCC9', '#12345', '#GGGGGG', '#1234567', '', 'rojo'):
            with self.assertRaises(ErrorDRF, msg=color):
                self.serializer.validate_color(color)


# ====================== PRUEBAS DE INTEGRACIÓN ======================

class CategoriasApiTests(CarpetaMediaTemporal, APITestCase):
    """CRUD de categorías: /api/categorias/"""

    def setUp(self):
        self.cliente = cliente_de(crear_admin())

    def _crear(self, **extra):
        datos = {'nombre': 'Saludos', 'color': '#2BBCC9', 'imagen': imagen_png()}
        datos.update(extra)
        return self.cliente.post('/api/categorias/', {k: v for k, v in datos.items() if v is not None})

    # ---- seguridad ----
    def test_sin_sesion_de_administrador_da_403(self):
        from rest_framework.test import APIClient
        self.assertEqual(APIClient().get('/api/categorias/').status_code, 403)

    # ---- listar ----
    def test_lista_ordenada_por_nombre_con_total_de_señas(self):
        familia = crear_categoria('Familia')
        saludos = crear_categoria('Saludos')
        crear_sena('Hola', saludos)
        crear_sena('Chau', saludos)
        lista = self.cliente.get('/api/categorias/').json()
        self.assertEqual([c['nombre'] for c in lista], ['Familia', 'Saludos'])
        self.assertEqual([c['total_senas'] for c in lista], [0, 2])
        self.assertEqual(lista[0]['id'], familia.pk)

    def test_la_imagen_se_devuelve_con_direccion_completa(self):
        crear_categoria('Saludos', imagen='/media/imagenes_senas/a.png')
        self.assertTrue(self.cliente.get('/api/categorias/').json()[0]['imagen'].startswith('http'))

    def test_categoria_sin_imagen_devuelve_null(self):
        crear_categoria('Saludos')
        self.assertIsNone(self.cliente.get('/api/categorias/').json()[0]['imagen'])

    # ---- crear ----
    def test_crear_con_imagen(self):
        r = self._crear()
        self.assertEqual(r.status_code, 201, r.content)
        self.assertEqual(r.data['data']['nombre'], 'Saludos')
        self.assertEqual(r.data['data']['total_senas'], 0)
        self.assertTrue(os.path.exists(ruta_en_disco(Categoria.objects.get().imagen)))

    def test_crear_sin_color_usa_el_color_por_defecto(self):
        r = self._crear(color=None)
        self.assertEqual(r.status_code, 201, r.content)
        self.assertEqual(Categoria.objects.get().color, '#2BBCC9')

    def test_crear_sin_imagen_da_error(self):
        r = self._crear(imagen=None)
        self.assertEqual(r.status_code, 400)
        self.assertIn('imagen', r.data['errores'])
        self.assertEqual(Categoria.objects.count(), 0)

    def test_crear_sin_nombre_da_error(self):
        r = self._crear(nombre='  ')
        self.assertEqual(r.status_code, 400)
        self.assertIn('nombre', r.data['errores'])

    def test_crear_con_nombre_repetido_da_error(self):
        crear_categoria('Saludos')
        r = self._crear(nombre='saludos')
        self.assertEqual(r.status_code, 400)
        self.assertIn('nombre', r.data['errores'])

    def test_crear_con_color_invalido_da_error(self):
        r = self._crear(color='azul')
        self.assertEqual(r.status_code, 400)
        self.assertIn('color', r.data['errores'])

    def test_crear_con_imagen_de_formato_no_permitido_da_error(self):
        gif = SimpleUploadedFile('a.gif', b'GIF89a', content_type='image/gif')
        r = self._crear(imagen=gif)
        self.assertEqual(r.status_code, 400)
        self.assertIn('imagen', r.data['errores'])

    # ---- editar ----
    def test_editar_el_nombre_actualiza_el_texto_de_sus_señas(self):
        """Spring Boot todavía lee sena.categoria como texto: debe seguir al nombre."""
        cat = crear_categoria('Saludos')
        sena = crear_sena('Hola', cat)
        r = self.cliente.patch(f'/api/categorias/{cat.pk}/', {'nombre': 'Saludos y cortesía'})
        self.assertEqual(r.status_code, 200, r.content)
        sena.refresh_from_db()
        self.assertEqual(sena.categoria, 'Saludos y cortesía')

    def test_editar_sin_imagen_conserva_la_anterior(self):
        cat = crear_categoria('Saludos', imagen='/media/imagenes_senas/vieja.png')
        self.cliente.patch(f'/api/categorias/{cat.pk}/', {'color': '#FF0000'})
        cat.refresh_from_db()
        self.assertEqual((cat.imagen, cat.color), ('/media/imagenes_senas/vieja.png', '#FF0000'))

    def test_editar_con_imagen_nueva_borra_la_vieja(self):
        vieja = guardar_imagen(imagen_png('vieja.png'))
        cat = crear_categoria('Saludos', imagen=vieja)
        r = self.cliente.patch(f'/api/categorias/{cat.pk}/', {'imagen': imagen_png('nueva.png')})
        self.assertEqual(r.status_code, 200, r.content)
        self.assertFalse(os.path.exists(ruta_en_disco(vieja)))
        cat.refresh_from_db()
        self.assertNotEqual(cat.imagen, vieja)
        self.assertTrue(os.path.exists(ruta_en_disco(cat.imagen)))

    def test_editar_no_permite_repetir_el_nombre_de_otra(self):
        crear_categoria('Saludos')
        otra = crear_categoria('Familia')
        r = self.cliente.patch(f'/api/categorias/{otra.pk}/', {'nombre': 'SALUDOS'})
        self.assertEqual(r.status_code, 400)

    def test_editar_conservando_su_propio_nombre_es_valido(self):
        cat = crear_categoria('Saludos')
        r = self.cliente.patch(f'/api/categorias/{cat.pk}/', {'nombre': 'Saludos', 'color': '#00FF00'})
        self.assertEqual(r.status_code, 200, r.content)

    # ---- eliminar ----
    def test_eliminar_una_categoria_sin_señas(self):
        cat = crear_categoria('Vacía', imagen=guardar_imagen(imagen_png()))
        archivo = ruta_en_disco(cat.imagen)
        r = self.cliente.delete(f'/api/categorias/{cat.pk}/')
        self.assertEqual(r.status_code, 200)
        self.assertEqual(r.data['senas_eliminadas'], 0)
        self.assertEqual(Categoria.objects.count(), 0)
        self.assertFalse(os.path.exists(archivo))

    def test_eliminar_una_categoria_elimina_sus_señas_en_cascada(self):
        cat = crear_categoria('Saludos', imagen=guardar_imagen(imagen_png('cat.png')))
        otra = crear_categoria('Familia')
        imagen_sena = guardar_imagen(imagen_png('sena.png'))
        hola = crear_sena('Hola', cat, imagen=imagen_sena)
        crear_sena('Chau', cat)
        mama = crear_sena('Mamá', otra)  # de otra categoría: no se toca
        guardar_video(hola, SimpleUploadedFile('v.mp4', b'0' * 32, content_type='video/mp4'))
        MuestraSena.objects.create(sena=hola, puntos='0')
        archivos = [ruta_en_disco(cat.imagen), ruta_en_disco(imagen_sena),
                    ruta_en_disco(VideoReferencia.objects.get().url_video)]
        self.assertTrue(all(os.path.exists(a) for a in archivos))

        r = self.cliente.delete(f'/api/categorias/{cat.pk}/')

        self.assertEqual(r.status_code, 200, r.content)
        self.assertEqual(r.data['senas_eliminadas'], 2)
        self.assertEqual(list(Categoria.objects.values_list('nombre', flat=True)), ['Familia'])
        self.assertEqual(list(Sena.objects.values_list('pk', flat=True)), [mama.pk])
        self.assertEqual((VideoReferencia.objects.count(), MuestraSena.objects.count()), (0, 0))
        self.assertFalse(any(os.path.exists(a) for a in archivos))

    def test_eliminar_conserva_el_historial_de_traducciones(self):
        cat = crear_categoria('Saludos')
        hola = crear_sena('Hola', cat)
        RegistroUso.objects.create(sena=hola, nombre_sena='Hola')
        self.cliente.delete(f'/api/categorias/{cat.pk}/')
        registro = RegistroUso.objects.get()
        self.assertIsNone(registro.sena)           # se desvincula de la seña…
        self.assertEqual(registro.nombre_sena, 'Hola')  # …pero queda el nombre

    def test_eliminar_una_categoria_que_no_existe_da_404(self):
        self.assertEqual(self.cliente.delete('/api/categorias/9999/').status_code, 404)

    # ---- señas de una categoría ----
    def test_ver_las_señas_de_una_categoria(self):
        cat = crear_categoria('Saludos')
        otra = crear_categoria('Familia')
        crear_sena('Hola', cat, imagen='/media/imagenes_senas/h.png', descripcion='Saludo')
        crear_sena('Mamá', otra)
        r = self.cliente.get(f'/api/categorias/{cat.pk}/senas/').json()
        self.assertEqual([s['nombre'] for s in r], ['Hola'])
        self.assertEqual(r[0]['descripcion'], 'Saludo')
        self.assertTrue(r[0]['imagen'].startswith('http'))

    def test_categoria_sin_señas_devuelve_lista_vacia(self):
        cat = crear_categoria('Vacía')
        self.assertEqual(self.cliente.get(f'/api/categorias/{cat.pk}/senas/').json(), [])
