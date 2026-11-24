"""Pruebas de la app senas (señas, login del administrador y entrenamiento).

Unitarias:   lógica sin base de datos (SimpleTestCase).
Integración: la API completa con una base de datos de pruebas (APITestCase).
"""
import os
from collections import namedtuple
from unittest.mock import patch

from django.core import signing
from django.core.exceptions import ValidationError
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import SimpleTestCase
from rest_framework.test import APIClient, APITestCase

from config.pruebas_comunes import (
    CarpetaMediaTemporal, cliente_de, crear_admin, crear_categoria, crear_sena, crear_usuario,
    imagen_png, puntos_mano, ruta_en_disco, video_mp4,
)

from .archivos import validar_imagen
from .auth import SAL, crear_token, es_admin, verificar_password
from .entrenamiento import N_CUADROS, puntos_a_texto, secuencia_a_texto
from .models import MuestraSena, MuestraSecuencia, Sena, Usuario, validar_video
from .progreso import META_MOVIMIENTOS, META_MUESTRAS, esta_entrenada, faltan

Archivo = namedtuple('Archivo', 'name size')  # archivo falso: solo nombre y tamaño
MB = 1024 * 1024


# ======================= PRUEBAS UNITARIAS =======================

class ProgresoUnitTests(SimpleTestCase):
    """Regla de negocio: cuándo una seña se considera 'Entrenada'."""

    def test_sin_entrenamiento_no_esta_entrenada(self):
        self.assertFalse(esta_entrenada(0, 0))

    def test_justo_debajo_de_las_metas_no_esta_entrenada(self):
        self.assertFalse(esta_entrenada(META_MUESTRAS - 1, META_MOVIMIENTOS - 1))

    def test_la_meta_de_muestras_basta(self):
        self.assertTrue(esta_entrenada(META_MUESTRAS, 0))

    def test_la_meta_de_movimientos_basta(self):
        self.assertTrue(esta_entrenada(0, META_MOVIMIENTOS))

    def test_faltan_resta_lo_guardado_de_cada_meta(self):
        self.assertEqual(faltan(1, 0), {'muestras': META_MUESTRAS - 1, 'movimientos': META_MOVIMIENTOS})

    def test_faltan_nunca_es_negativo(self):
        self.assertEqual(faltan(50, 99), {'muestras': 0, 'movimientos': 0})

    def test_las_metas_son_2_muestras_y_2_movimientos(self):
        self.assertEqual((META_MUESTRAS, META_MOVIMIENTOS), (2, 2))


class ValidarImagenUnitTests(SimpleTestCase):
    def test_acepta_jpg_jpeg_y_png(self):
        for nombre in ('a.jpg', 'a.jpeg', 'a.png', 'FOTO.PNG'):
            validar_imagen(Archivo(nombre, 1000))  # no debe lanzar error

    def test_rechaza_otros_formatos(self):
        for nombre in ('a.gif', 'a.pdf', 'a.webp', 'sin_extension'):
            with self.assertRaises(ValidationError, msg=nombre):
                validar_imagen(Archivo(nombre, 1000))

    def test_acepta_exactamente_2_mb(self):
        validar_imagen(Archivo('a.png', 2 * MB))

    def test_rechaza_mas_de_2_mb(self):
        with self.assertRaises(ValidationError):
            validar_imagen(Archivo('a.png', 2 * MB + 1))


class ValidarVideoUnitTests(SimpleTestCase):
    def test_acepta_mp4_hasta_50_mb(self):
        validar_video(Archivo('clip.mp4', 50 * MB))

    def test_rechaza_otros_formatos(self):
        with self.assertRaises(ValidationError):
            validar_video(Archivo('clip.avi', 1000))

    def test_rechaza_mas_de_50_mb(self):
        with self.assertRaises(ValidationError):
            validar_video(Archivo('clip.mp4', 50 * MB + 1))


class AuthUnitTests(SimpleTestCase):
    def test_verificar_password_correcta_e_incorrecta(self):
        import bcrypt
        hash_ = bcrypt.hashpw(b'secreta', bcrypt.gensalt(rounds=4)).decode()
        self.assertTrue(verificar_password('secreta', hash_))
        self.assertFalse(verificar_password('otra', hash_))

    def test_hash_invalido_no_rompe_y_rechaza(self):
        self.assertFalse(verificar_password('x', 'esto-no-es-un-hash'))

    def test_es_admin_acepta_los_dos_roles_sin_importar_mayusculas(self):
        for rol in ('admin', 'ADMIN', 'administrador', 'Administrador'):
            self.assertTrue(es_admin(Usuario(rol=rol)), rol)

    def test_es_admin_rechaza_otros_roles(self):
        for rol in ('usuario', 'user', ''):
            self.assertFalse(es_admin(Usuario(rol=rol)), rol)

    def test_el_token_guarda_el_id_y_esta_firmado(self):
        token = crear_token(Usuario(id_usuario=7))
        self.assertEqual(signing.loads(token, salt=SAL)['id'], 7)

    def test_un_token_alterado_no_es_valido(self):
        token = crear_token(Usuario(id_usuario=7))
        with self.assertRaises(signing.BadSignature):
            signing.loads(token[:-2] + 'xx', salt=SAL)


class EntrenamientoUnitTests(SimpleTestCase):
    """Conversión de los puntos de la mano a texto (lo que se guarda para el KNN/DTW)."""

    def test_muestra_tiene_63_valores_normalizados(self):
        valores = [float(v) for v in puntos_a_texto(puntos_mano()).split(',')]
        self.assertEqual(len(valores), 21 * 3)
        self.assertTrue(all(-1.0 <= v <= 1.0 for v in valores))

    def test_muestra_es_relativa_al_primer_punto(self):
        valores = [float(v) for v in puntos_a_texto(puntos_mano(desplazamiento=5.0)).split(',')]
        self.assertEqual(valores[:3], [0.0, 0.0, 0.0])

    def test_la_posicion_de_la_mano_no_cambia_la_muestra(self):
        a = [float(v) for v in puntos_a_texto(puntos_mano(0.0)).split(',')]
        b = [float(v) for v in puntos_a_texto(puntos_mano(3.0)).split(',')]
        for x, y in zip(a, b):
            self.assertAlmostEqual(x, y, places=9)

    def test_secuencia_se_lleva_a_20_cuadros(self):
        cuadros = [puntos_mano(i / 100) for i in range(12)]
        filas = secuencia_a_texto(cuadros).split(';')
        self.assertEqual(len(filas), N_CUADROS)
        self.assertEqual(len(filas[0].split(',')), 65)  # 21 puntos x 3 + recorrido (x, y) de la muñeca


# ====================== PRUEBAS DE INTEGRACIÓN ======================

class LoginApiTests(APITestCase):
    """POST /api/login/ — único acceso del administrador (lo usa Spring Boot)."""

    def setUp(self):
        self.admin = crear_admin()

    def _login(self, email='admin@senasapp.pe', password='admin123'):
        return self.client.post('/api/login/', {'email': email, 'password': password}, format='json')

    def test_login_correcto_devuelve_token_y_datos(self):
        r = self._login()
        self.assertEqual(r.status_code, 200)
        self.assertTrue(r.data['exito'])
        self.assertTrue(r.data['token'])
        self.assertEqual(r.data['usuario'], {'nombre': 'Administrador', 'email': 'admin@senasapp.pe'})

    def test_el_correo_no_distingue_mayusculas(self):
        self.assertEqual(self._login(email='ADMIN@SENASAPP.PE').status_code, 200)

    def test_password_incorrecta_da_401(self):
        self.assertEqual(self._login(password='mala').status_code, 401)

    def test_correo_inexistente_da_401(self):
        self.assertEqual(self._login(email='nadie@correo.com').status_code, 401)

    def test_cuenta_desactivada_da_403(self):
        Usuario.objects.filter(pk=self.admin.pk).update(activo=False)
        self.assertEqual(self._login().status_code, 403)

    def test_usuario_comun_no_puede_entrar_al_panel(self):
        crear_usuario(email='ana@correo.com', rol='usuario', password='clave123')
        r = self._login(email='ana@correo.com', password='clave123')
        self.assertEqual(r.status_code, 403)

    def test_el_token_recibido_sirve_para_usar_la_api(self):
        token = self._login().data['token']
        r = APIClient().get('/api/senas/', HTTP_AUTHORIZATION=f'Bearer {token}')
        self.assertEqual(r.status_code, 200)


class PermisosApiTests(APITestCase):
    """Solo un administrador activo con sesión vigente puede usar la API."""

    def setUp(self):
        self.admin = crear_admin()

    def test_sin_token_da_403(self):
        self.assertEqual(APIClient().get('/api/senas/').status_code, 403)

    def test_token_falso_da_403(self):
        r = APIClient().get('/api/senas/', HTTP_AUTHORIZATION='Bearer token-falso')
        self.assertEqual(r.status_code, 403)

    def test_token_de_usuario_comun_da_403(self):
        ana = crear_usuario(email='ana@correo.com', rol='usuario')
        self.assertEqual(cliente_de(ana).get('/api/senas/').status_code, 403)

    def test_admin_desactivado_da_403(self):
        Usuario.objects.filter(pk=self.admin.pk).update(activo=False)
        self.assertEqual(cliente_de(self.admin).get('/api/senas/').status_code, 403)

    def test_token_vencido_da_403(self):
        cliente = cliente_de(self.admin)
        with patch('senas.auth.DURACION_S', -1):
            self.assertEqual(cliente.get('/api/senas/').status_code, 403)

    def test_admin_con_sesion_vigente_puede_entrar(self):
        self.assertEqual(cliente_de(self.admin).get('/api/senas/').status_code, 200)

    def test_el_mensaje_de_rechazo_explica_el_motivo(self):
        r = APIClient().get('/api/senas/')
        self.assertIn('vencida', r.data['detail'])


class SenasApiTests(CarpetaMediaTemporal, APITestCase):
    """CRUD de señas: /api/senas/"""

    def setUp(self):
        self.cliente = cliente_de(crear_admin())
        self.cat = crear_categoria('Saludos')

    def _crear(self, **extra):
        datos = {'nombre': 'Buenos días', 'categoria_id': self.cat.pk,
                 'descripcion': 'Mano abierta', 'imagen': imagen_png()}
        datos.update(extra)
        return self.cliente.post('/api/senas/', {k: v for k, v in datos.items() if v is not None})

    # ---- listar ----
    def test_lista_vacia_es_un_arreglo(self):
        r = self.cliente.get('/api/senas/')
        self.assertEqual((r.status_code, r.json()), (200, []))

    def test_lista_devuelve_los_campos_que_usa_el_panel(self):
        crear_sena('Hola', self.cat, imagen='/media/imagenes_senas/x.png')
        sena = self.cliente.get('/api/senas/').json()[0]
        for campo in ('id', 'nombre', 'categoria', 'categoria_color', 'imagen', 'video',
                      'muestras', 'movimientos', 'entrenada', 'faltan', 'es_nueva'):
            self.assertIn(campo, sena)
        self.assertEqual(sena['categoria'], 'Saludos')
        self.assertTrue(sena['imagen'].startswith('http'))  # dirección completa

    def test_lista_indica_cuanto_falta_para_entrenar(self):
        hola = crear_sena('Hola', self.cat)
        MuestraSena.objects.create(sena=hola, puntos='0')
        MuestraSecuencia.objects.create(sena=hola, cuadros='0', num_cuadros=10)
        sena = self.cliente.get('/api/senas/').json()[0]
        self.assertEqual(sena['faltan'], {'muestras': META_MUESTRAS - 1, 'movimientos': META_MOVIMIENTOS - 1})

    def test_filtra_por_texto(self):
        crear_sena('Hola', self.cat, descripcion='saludo simple')
        crear_sena('Gracias', self.cat)
        nombres = [s['nombre'] for s in self.cliente.get('/api/senas/', {'q': 'hol'}).json()]
        self.assertEqual(nombres, ['Hola'])

    def test_filtra_por_categoria(self):
        otra = crear_categoria('Familia')
        crear_sena('Hola', self.cat)
        crear_sena('Mamá', otra)
        nombres = [s['nombre'] for s in self.cliente.get('/api/senas/', {'categoria': otra.pk}).json()]
        self.assertEqual(nombres, ['Mamá'])

    def test_estado_entrenada_se_calcula_con_las_muestras(self):
        lista = crear_sena('Lista', self.cat)
        crear_sena('Pendiente', self.cat)
        MuestraSena.objects.bulk_create([MuestraSena(sena=lista, puntos='0') for _ in range(META_MUESTRAS)])
        r = self.cliente.get('/api/senas/', {'estado': 'entrenada'}).json()
        self.assertEqual([s['nombre'] for s in r], ['Lista'])
        self.assertTrue(r[0]['entrenada'])
        r = self.cliente.get('/api/senas/', {'estado': 'sin_entrenar'}).json()
        self.assertEqual([s['nombre'] for s in r], ['Pendiente'])

    # ---- crear ----
    def test_crear_seña_con_imagen(self):
        r = self._crear()
        self.assertEqual(r.status_code, 201, r.content)
        self.assertTrue(r.data['exito'])
        data = r.data['data']
        self.assertEqual(data['nombre'], 'Buenos días')
        self.assertEqual(data['categoria'], 'Saludos')
        self.assertFalse(data['entrenada'])
        self.assertEqual(data['muestras'], {'actual': 0, 'meta': META_MUESTRAS})
        self.assertTrue(os.path.exists(ruta_en_disco(Sena.objects.get().imagen)))

    def test_crear_guarda_el_texto_de_la_categoria_para_spring(self):
        self._crear()
        self.assertEqual(Sena.objects.get().categoria, 'Saludos')

    def test_crear_con_video(self):
        r = self._crear(video=video_mp4())
        self.assertEqual(r.status_code, 201, r.content)
        self.assertTrue(r.data['data']['video'].endswith('.mp4'))

    def test_crear_sin_imagen_da_error(self):
        r = self._crear(imagen=None)
        self.assertEqual(r.status_code, 400)
        self.assertIn('imagen', r.data['errores'])
        self.assertEqual(Sena.objects.count(), 0)

    def test_crear_sin_nombre_da_error(self):
        r = self._crear(nombre='   ')
        self.assertEqual(r.status_code, 400)
        self.assertIn('nombre', r.data['errores'])

    def test_crear_sin_categoria_da_error(self):
        r = self._crear(categoria_id=None)
        self.assertEqual(r.status_code, 400)
        self.assertIn('categoria_id', r.data['errores'])

    def test_crear_con_nombre_repetido_da_error(self):
        crear_sena('Buenos días', self.cat)
        r = self._crear(nombre='buenos días')
        self.assertEqual(r.status_code, 400)
        self.assertIn('nombre', r.data['errores'])

    def test_crear_con_imagen_de_formato_no_permitido(self):
        gif = SimpleUploadedFile('a.gif', b'GIF89a', content_type='image/gif')
        r = self._crear(imagen=gif)
        self.assertEqual(r.status_code, 400)
        self.assertIn('imagen', r.data['errores'])

    def test_crear_con_video_que_no_es_mp4(self):
        avi = SimpleUploadedFile('a.avi', b'0000', content_type='video/avi')
        r = self._crear(video=avi)
        self.assertEqual(r.status_code, 400)
        self.assertIn('video', r.data['errores'])

    # ---- editar ----
    def test_editar_cambia_nombre_y_categoria(self):
        otra = crear_categoria('Familia')
        sena = crear_sena('Mama', self.cat)
        r = self.cliente.patch(f'/api/senas/{sena.pk}/', {'nombre': 'Mamá', 'categoria_id': otra.pk})
        self.assertEqual(r.status_code, 200, r.content)
        sena.refresh_from_db()
        self.assertEqual((sena.nombre, sena.categoria, sena.categoria_ref_id), ('Mamá', 'Familia', otra.pk))

    def test_editar_sin_imagen_conserva_la_anterior(self):
        sena = crear_sena('Hola', self.cat, imagen='/media/imagenes_senas/vieja.png')
        self.cliente.patch(f'/api/senas/{sena.pk}/', {'descripcion': 'nueva'})
        sena.refresh_from_db()
        self.assertEqual(sena.imagen, '/media/imagenes_senas/vieja.png')

    def test_editar_con_imagen_nueva_borra_la_vieja(self):
        creada = self._crear().data['data']
        vieja = ruta_en_disco(Sena.objects.get(pk=creada['id']).imagen)
        self.assertTrue(os.path.exists(vieja))
        r = self.cliente.patch(f"/api/senas/{creada['id']}/", {'imagen': imagen_png('nueva.png')})
        self.assertEqual(r.status_code, 200, r.content)
        self.assertFalse(os.path.exists(vieja))
        self.assertTrue(os.path.exists(ruta_en_disco(Sena.objects.get(pk=creada['id']).imagen)))

    def test_editar_puede_quitar_el_video(self):
        creada = self._crear(video=video_mp4()).data['data']
        self.assertIsNotNone(creada['video'])
        r = self.cliente.patch(f"/api/senas/{creada['id']}/", {'quitar_video': 'true'})
        self.assertIsNone(r.data['data']['video'])

    def test_editar_no_permite_repetir_el_nombre_de_otra_seña(self):
        crear_sena('Hola', self.cat)
        otra = crear_sena('Gracias', self.cat)
        r = self.cliente.patch(f'/api/senas/{otra.pk}/', {'nombre': 'hola'})
        self.assertEqual(r.status_code, 400)

    # ---- eliminar ----
    def test_eliminar_borra_la_seña_y_su_imagen(self):
        creada = self._crear().data['data']
        archivo = ruta_en_disco(Sena.objects.get(pk=creada['id']).imagen)
        r = self.cliente.delete(f"/api/senas/{creada['id']}/")
        self.assertEqual(r.status_code, 200)
        self.assertEqual(Sena.objects.count(), 0)
        self.assertFalse(os.path.exists(archivo))

    def test_eliminar_una_seña_que_no_existe_da_404(self):
        self.assertEqual(self.cliente.delete('/api/senas/9999/').status_code, 404)


class EntrenamientoApiTests(APITestCase):
    """Captura de muestras y movimientos: /api/entrenamiento/…"""

    def setUp(self):
        self.cliente = cliente_de(crear_admin())
        self.sena = crear_sena('Hola', crear_categoria())

    def test_guarda_una_muestra_de_21_puntos(self):
        r = self.cliente.post(f'/api/entrenamiento/muestras/{self.sena.pk}/', puntos_mano(), format='json')
        self.assertEqual(r.status_code, 201, r.content)
        self.assertEqual(MuestraSena.objects.filter(sena=self.sena).count(), 1)

    def test_rechaza_una_muestra_con_cantidad_de_puntos_invalida(self):
        r = self.cliente.post(f'/api/entrenamiento/muestras/{self.sena.pk}/', puntos_mano()[:5], format='json')
        self.assertEqual(r.status_code, 400)
        self.assertEqual(MuestraSena.objects.count(), 0)

    def test_muestra_de_una_seña_inexistente_da_404(self):
        r = self.cliente.post('/api/entrenamiento/muestras/9999/', puntos_mano(), format='json')
        self.assertEqual(r.status_code, 404)

    def test_guarda_un_movimiento_de_10_cuadros(self):
        cuadros = [puntos_mano(i / 100) for i in range(10)]
        r = self.cliente.post(f'/api/entrenamiento/secuencias/{self.sena.pk}/', cuadros, format='json')
        self.assertEqual(r.status_code, 201, r.content)
        self.assertEqual(MuestraSecuencia.objects.get().num_cuadros, 10)

    def test_rechaza_un_movimiento_con_pocos_cuadros(self):
        cuadros = [puntos_mano() for _ in range(3)]
        r = self.cliente.post(f'/api/entrenamiento/secuencias/{self.sena.pk}/', cuadros, format='json')
        self.assertEqual(r.status_code, 400)

    def test_resumen_cuenta_muestras_y_movimientos(self):
        MuestraSena.objects.bulk_create([MuestraSena(sena=self.sena, puntos='0') for _ in range(3)])
        MuestraSecuencia.objects.create(sena=self.sena, cuadros='0', num_cuadros=10)
        fila = self.cliente.get('/api/entrenamiento/').json()[0]
        self.assertEqual((fila['nombre'], fila['muestras'], fila['secuencias']), ('Hola', 3, 1))

    def test_borrar_solo_las_muestras(self):
        MuestraSena.objects.create(sena=self.sena, puntos='0')
        MuestraSecuencia.objects.create(sena=self.sena, cuadros='0', num_cuadros=10)
        r = self.cliente.delete(f'/api/entrenamiento/{self.sena.pk}/?tipo=muestras')
        self.assertEqual(r.status_code, 200)
        self.assertEqual((MuestraSena.objects.count(), MuestraSecuencia.objects.count()), (0, 1))

    def test_borrar_todo_el_entrenamiento(self):
        MuestraSena.objects.create(sena=self.sena, puntos='0')
        MuestraSecuencia.objects.create(sena=self.sena, cuadros='0', num_cuadros=10)
        self.cliente.delete(f'/api/entrenamiento/{self.sena.pk}/')
        self.assertEqual((MuestraSena.objects.count(), MuestraSecuencia.objects.count()), (0, 0))

    def test_predecir_exige_21_puntos(self):
        r = self.cliente.post('/api/predecir/', {'landmarks': puntos_mano()[:4]}, format='json')
        self.assertEqual(r.status_code, 400)
