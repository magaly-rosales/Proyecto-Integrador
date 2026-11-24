"""Pruebas de la app usuarios (lista, activar/desactivar cuentas y perfil del administrador)."""
import os

from django.test import SimpleTestCase
from rest_framework.test import APIClient, APITestCase

from django.core.files.uploadedfile import SimpleUploadedFile

from config.pruebas_comunes import (
    CarpetaMediaTemporal, cliente_de, crear_admin, crear_usuario, imagen_png, ruta_en_disco,
)
from senas.models import Usuario

from senas.auth import verificar_password

from .perfil import (
    MIN_PASSWORD, ErrorPerfil, datos_perfil, validar_password_nueva,
)
from .serializers import UsuarioSerializer


# ======================= PRUEBAS UNITARIAS =======================

class UsuarioSerializerUnitTests(SimpleTestCase):
    def setUp(self):
        self.serializer = UsuarioSerializer()

    def test_iniciales_son_las_de_las_dos_primeras_palabras(self):
        self.assertEqual(self.serializer.get_iniciales(Usuario(nombre='Ana María Pérez')), 'AM')

    def test_iniciales_con_un_solo_nombre(self):
        self.assertEqual(self.serializer.get_iniciales(Usuario(nombre='luis')), 'L')

    def test_iniciales_de_nombre_vacio(self):
        self.assertEqual(self.serializer.get_iniciales(Usuario(nombre='   ')), '')

    def test_foto_vacia_devuelve_none(self):
        for valor in (None, '', '   '):
            self.assertIsNone(self.serializer.get_foto(Usuario(foto=valor)))

    def test_foto_con_ruta_se_devuelve_tal_cual_sin_request(self):
        self.assertEqual(self.serializer.get_foto(Usuario(foto='/media/fotos/a.png')), '/media/fotos/a.png')

    def test_foto_con_url_completa_no_se_modifica(self):
        self.assertEqual(self.serializer.get_foto(Usuario(foto='https://x.pe/a.png')), 'https://x.pe/a.png')

    def test_is_staff_indica_si_es_administrador(self):
        self.assertTrue(self.serializer.get_is_staff(Usuario(rol='admin')))
        self.assertFalse(self.serializer.get_is_staff(Usuario(rol='usuario')))


class PerfilUnitTests(SimpleTestCase):
    """Reglas del perfil del administrador (sin base de datos)."""

    def _campo_del_error(self, funcion, *args):
        with self.assertRaises(ErrorPerfil) as contexto:
            funcion(*args)
        return contexto.exception.campo

    def test_password_corta_da_error_en_nueva(self):
        corta = 'a' * (MIN_PASSWORD - 1)
        self.assertEqual(self._campo_del_error(validar_password_nueva, 'actual123', corta, corta), 'nueva')

    def test_password_igual_a_la_actual_da_error_en_nueva(self):
        self.assertEqual(
            self._campo_del_error(validar_password_nueva, 'misma-clave1', 'misma-clave1', 'misma-clave1'), 'nueva')

    def test_confirmacion_distinta_da_error_en_confirmar(self):
        self.assertEqual(
            self._campo_del_error(validar_password_nueva, 'actual123', 'nueva-clave1', 'otra-clave1'), 'confirmar')

    def test_password_valida_no_lanza_error(self):
        validar_password_nueva('actual123', 'nueva-clave1', 'nueva-clave1')

    def test_password_sin_numero_da_error_en_nueva(self):
        self.assertEqual(
            self._campo_del_error(validar_password_nueva, 'actual123', 'solo-letras', 'solo-letras'), 'nueva')

    def test_password_sin_letra_da_error_en_nueva(self):
        self.assertEqual(
            self._campo_del_error(validar_password_nueva, 'actual123', '1234567890', '1234567890'), 'nueva')

    def test_datos_perfil_trae_nombre_correo_rol_iniciales_y_foto(self):
        datos = datos_perfil(Usuario(nombre='Magaly Rosales', email='m@senasapp.pe', rol='admin'))
        self.assertEqual(datos, {'nombre': 'Magaly Rosales', 'email': 'm@senasapp.pe',
                                 'rol': 'Administrador', 'iniciales': 'MR', 'foto': None})


# ====================== PRUEBAS DE INTEGRACIÓN ======================

class PerfilApiTests(CarpetaMediaTemporal, APITestCase):
    """/api/perfil/, /api/perfil/foto/ y /api/perfil/password/"""

    def setUp(self):
        self.admin = crear_admin()  # contraseña: admin123
        self.cliente = cliente_de(self.admin)

    def _password(self, actual='admin123', nueva='nueva-clave1', confirmar='nueva-clave1'):
        return self.cliente.post('/api/perfil/password/',
                                 {'actual': actual, 'nueva': nueva, 'confirmar': confirmar}, format='json')

    def test_sin_sesion_de_administrador_da_403(self):
        self.assertEqual(APIClient().get('/api/perfil/').status_code, 403)
        self.assertEqual(APIClient().post('/api/perfil/password/', {}, format='json').status_code, 403)

    def test_usuario_comun_no_puede_ver_el_perfil(self):
        ana = crear_usuario('Ana Torres', 'ana@correo.com')
        self.assertEqual(cliente_de(ana).get('/api/perfil/').status_code, 403)

    def test_devuelve_los_datos_del_administrador_con_sesion(self):
        datos = self.cliente.get('/api/perfil/').json()
        self.assertEqual((datos['nombre'], datos['email'], datos['rol']),
                         ('Administrador', 'admin@senasapp.pe', 'Administrador'))
        self.assertNotIn('password_hash', datos)

    def test_el_nombre_el_correo_y_el_rol_no_se_pueden_cambiar_desde_el_perfil(self):
        r = self.cliente.patch('/api/perfil/', {'nombre': 'Otro', 'email': 'otro@correo.com', 'rol': 'usuario'},
                               format='json')
        self.assertEqual(r.status_code, 405)
        self.admin.refresh_from_db()
        self.assertEqual((self.admin.nombre, self.admin.email, self.admin.rol),
                         ('Administrador', 'admin@senasapp.pe', 'admin'))

    # ---- foto ----
    def _subir_foto(self, archivo=None):
        return self.cliente.post('/api/perfil/foto/', {'foto': archivo or imagen_png()}, format='multipart')

    def test_sin_foto_el_perfil_devuelve_none(self):
        self.assertIsNone(self.cliente.get('/api/perfil/').json()['foto'])

    def test_agregar_foto_la_guarda_y_devuelve_la_direccion_completa(self):
        r = self._subir_foto()
        self.assertEqual(r.status_code, 200, r.content)
        self.assertTrue(r.data['data']['foto'].startswith('http'))
        self.admin.refresh_from_db()
        self.assertTrue(os.path.exists(ruta_en_disco(self.admin.foto)))

    def test_cambiar_la_foto_borra_la_anterior(self):
        self._subir_foto()
        self.admin.refresh_from_db()
        vieja = ruta_en_disco(self.admin.foto)
        self._subir_foto(imagen_png('otra.png'))
        self.assertFalse(os.path.exists(vieja))

    def test_quitar_la_foto_la_borra(self):
        self._subir_foto()
        self.admin.refresh_from_db()
        archivo = ruta_en_disco(self.admin.foto)
        r = self.cliente.delete('/api/perfil/foto/')
        self.assertEqual(r.status_code, 200, r.content)
        self.assertIsNone(r.data['data']['foto'])
        self.assertFalse(os.path.exists(archivo))
        self.admin.refresh_from_db()
        self.assertIsNone(self.admin.foto)

    def test_quitar_foto_sin_tener_foto_no_falla(self):
        self.assertEqual(self.cliente.delete('/api/perfil/foto/').status_code, 200)

    def test_foto_con_formato_no_permitido_da_400(self):
        gif = SimpleUploadedFile('a.gif', b'GIF89a', content_type='image/gif')
        r = self._subir_foto(gif)
        self.assertEqual((r.status_code, r.data['campo']), (400, 'foto'))

    def test_subir_sin_archivo_da_400(self):
        r = self.cliente.post('/api/perfil/foto/', {}, format='multipart')
        self.assertEqual((r.status_code, r.data['campo']), (400, 'foto'))

    def test_la_foto_exige_sesion_de_administrador(self):
        self.assertEqual(APIClient().delete('/api/perfil/foto/').status_code, 403)

    def test_cambiar_la_contraseña_guarda_el_nuevo_hash(self):
        r = self._password()
        self.assertEqual(r.status_code, 200, r.content)
        self.admin.refresh_from_db()
        self.assertTrue(verificar_password('nueva-clave1', self.admin.password_hash))
        self.assertFalse(verificar_password('admin123', self.admin.password_hash))

    def test_con_la_contraseña_nueva_se_puede_iniciar_sesion(self):
        self._password()
        r = APIClient().post('/api/login/', {'email': 'admin@senasapp.pe', 'password': 'nueva-clave1'},
                             format='json')
        self.assertEqual(r.status_code, 200)

    def test_contraseña_actual_incorrecta_da_400_y_no_cambia(self):
        r = self._password(actual='mala')
        self.assertEqual((r.status_code, r.data['campo']), (400, 'actual'))
        self.admin.refresh_from_db()
        self.assertTrue(verificar_password('admin123', self.admin.password_hash))

    def test_contraseña_nueva_corta_da_400(self):
        r = self._password(nueva='corta', confirmar='corta')
        self.assertEqual((r.status_code, r.data['campo']), (400, 'nueva'))

    def test_contraseña_nueva_sin_numero_da_400(self):
        r = self._password(nueva='solo-letras', confirmar='solo-letras')
        self.assertEqual((r.status_code, r.data['campo']), (400, 'nueva'))

    def test_confirmacion_distinta_da_400(self):
        r = self._password(confirmar='otra-distinta')
        self.assertEqual((r.status_code, r.data['campo']), (400, 'confirmar'))

    def test_faltan_campos_da_400(self):
        r = self.cliente.post('/api/perfil/password/', {}, format='json')
        self.assertEqual(r.status_code, 400)


class UsuariosApiTests(APITestCase):
    """/api/usuarios/"""

    def setUp(self):
        self.admin = crear_admin()
        self.cliente = cliente_de(self.admin)
        self.ana = crear_usuario('Ana Torres', 'ana@correo.com')
        self.luis = crear_usuario('Luis Ríos', 'luis@gmail.com', activo=False)

    def _nombres(self, **filtros):
        return [u['nombre'] for u in self.cliente.get('/api/usuarios/', filtros).json()]

    def test_sin_sesion_de_administrador_da_403(self):
        self.assertEqual(APIClient().get('/api/usuarios/').status_code, 403)

    def test_la_lista_no_incluye_a_los_administradores(self):
        crear_usuario('Otro Admin', 'otro@senasapp.pe', rol='Administrador')
        self.assertEqual(self._nombres(), ['Ana Torres', 'Luis Ríos'])

    def test_la_lista_trae_los_campos_que_usa_el_panel(self):
        ana = self.cliente.get('/api/usuarios/').json()[0]
        self.assertEqual(ana['id'], self.ana.pk)
        self.assertEqual((ana['iniciales'], ana['is_active'], ana['is_staff']), ('AT', True, False))
        self.assertNotIn('password_hash', ana)  # nunca se expone la contraseña

    def test_la_lista_incluye_la_foto_con_direccion_completa(self):
        Usuario.objects.filter(pk=self.ana.pk).update(foto='/media/fotos/ana.png')
        ana = self.cliente.get('/api/usuarios/', {'q': 'ana'}).json()[0]
        self.assertTrue(ana['foto'].startswith('http'))
        self.assertIsNone(self.cliente.get('/api/usuarios/', {'q': 'luis'}).json()[0]['foto'])

    def test_busca_por_nombre(self):
        self.assertEqual(self._nombres(q='torres'), ['Ana Torres'])

    def test_busca_por_correo(self):
        self.assertEqual(self._nombres(q='gmail'), ['Luis Ríos'])

    def test_filtra_por_estado(self):
        self.assertEqual(self._nombres(estado='activo'), ['Ana Torres'])
        self.assertEqual(self._nombres(estado='inactivo'), ['Luis Ríos'])

    def test_desactivar_una_cuenta(self):
        r = self.cliente.patch(f'/api/usuarios/{self.ana.pk}/', {'is_active': False}, format='json')
        self.assertEqual(r.status_code, 200, r.content)
        self.assertFalse(r.data['data']['is_active'])
        self.ana.refresh_from_db()
        self.assertFalse(self.ana.activo)

    def test_activar_una_cuenta(self):
        self.cliente.patch(f'/api/usuarios/{self.luis.pk}/', {'is_active': True}, format='json')
        self.luis.refresh_from_db()
        self.assertTrue(self.luis.activo)

    def test_pedir_is_active_invalido_da_400(self):
        for valor in ('quizas', None):
            r = self.cliente.patch(f'/api/usuarios/{self.ana.pk}/', {'is_active': valor}, format='json')
            self.assertEqual(r.status_code, 400, valor)
        self.ana.refresh_from_db()
        self.assertTrue(self.ana.activo)

    def test_el_panel_no_puede_cambiar_nombre_ni_rol(self):
        self.cliente.patch(f'/api/usuarios/{self.ana.pk}/',
                           {'is_active': True, 'nombre': 'Hackeada', 'rol': 'admin'}, format='json')
        self.ana.refresh_from_db()
        self.assertEqual((self.ana.nombre, self.ana.rol), ('Ana Torres', 'usuario'))

    def test_un_administrador_no_se_puede_desactivar_desde_el_panel(self):
        r = self.cliente.patch(f'/api/usuarios/{self.admin.pk}/', {'is_active': False}, format='json')
        self.assertEqual(r.status_code, 404)
        self.admin.refresh_from_db()
        self.assertTrue(self.admin.activo)

    def test_no_se_pueden_crear_ni_borrar_usuarios_desde_el_panel(self):
        self.assertEqual(self.cliente.post('/api/usuarios/', {'nombre': 'X'}, format='json').status_code, 405)
        self.assertEqual(self.cliente.delete(f'/api/usuarios/{self.ana.pk}/').status_code, 405)

    def test_una_cuenta_desactivada_deja_de_poder_iniciar_sesion_en_el_panel(self):
        admin2 = crear_usuario('Admin 2', 'admin2@senasapp.pe', rol='admin', password='clave456')
        cliente = cliente_de(admin2)
        Usuario.objects.filter(pk=admin2.pk).update(activo=False)
        self.assertEqual(cliente.get('/api/usuarios/').status_code, 403)
