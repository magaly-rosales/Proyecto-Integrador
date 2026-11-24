"""Pruebas de la app usuarios (lista y activar/desactivar cuentas)."""
from django.test import SimpleTestCase
from rest_framework.test import APIClient, APITestCase

from config.pruebas_comunes import cliente_de, crear_admin, crear_usuario
from senas.models import Usuario

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

    def test_is_staff_indica_si_es_administrador(self):
        self.assertTrue(self.serializer.get_is_staff(Usuario(rol='admin')))
        self.assertFalse(self.serializer.get_is_staff(Usuario(rol='usuario')))


# ====================== PRUEBAS DE INTEGRACIÓN ======================

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
