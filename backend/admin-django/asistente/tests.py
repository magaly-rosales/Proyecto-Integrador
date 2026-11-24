"""Pruebas del asistente con IA (la API de Gemini siempre se simula: las pruebas no usan internet ni la clave).

Unitarias:   formato de los mensajes y conexión con Gemini (SimpleTestCase).
Integración: herramientas con datos reales de la base, conversación y API (APITestCase).
"""
import json
import urllib.error
from unittest.mock import patch

from django.test import SimpleTestCase, override_settings
from rest_framework.test import APIClient, APITestCase

from config.pruebas_comunes import cliente_de, crear_admin, crear_categoria, crear_sena, crear_usuario
from senas.models import MuestraSena, RegistroUso
from senas.progreso import META_MUESTRAS

from . import gemini, herramientas, servicio
from .gemini import ErrorGemini
from .herramientas import Contexto


def texto(contenido):
    """Respuesta de Gemini con solo texto."""
    return {'candidates': [{'content': {'parts': [{'text': contenido}]}}]}


def llamada(nombre, argumentos=None):
    """Respuesta de Gemini que pide usar una herramienta."""
    return {'candidates': [{'content': {'parts': [{'functionCall': {'name': nombre, 'args': argumentos or {}}}]}}]}


# ======================= PRUEBAS UNITARIAS =======================

class MensajesUnitTests(SimpleTestCase):
    def test_convierte_los_roles_al_formato_de_gemini(self):
        contenidos = servicio.a_contenidos([
            {'rol': 'usuario', 'texto': 'Hola'},
            {'rol': 'asistente', 'texto': '¿En qué te ayudo?'},
            {'rol': 'usuario', 'texto': '¿Cuántos usuarios hay?'},
        ])
        self.assertEqual([c['role'] for c in contenidos], ['user', 'model', 'user'])
        self.assertEqual(contenidos[2]['parts'], [{'text': '¿Cuántos usuarios hay?'}])

    def test_sin_mensajes_da_error(self):
        for valor in (None, [], 'hola'):
            with self.assertRaises(ValueError, msg=valor):
                servicio.a_contenidos(valor)

    def test_rol_invalido_da_error(self):
        with self.assertRaises(ValueError):
            servicio.a_contenidos([{'rol': 'sistema', 'texto': 'hola'}])

    def test_mensaje_vacio_da_error(self):
        with self.assertRaises(ValueError):
            servicio.a_contenidos([{'rol': 'usuario', 'texto': '   '}])

    def test_el_ultimo_mensaje_debe_ser_del_administrador(self):
        with self.assertRaises(ValueError):
            servicio.a_contenidos([{'rol': 'usuario', 'texto': 'Hola'}, {'rol': 'asistente', 'texto': 'Hola'}])

    def test_descarta_mensajes_iniciales_del_asistente(self):
        contenidos = servicio.a_contenidos([
            {'rol': 'asistente', 'texto': 'Bienvenido'},
            {'rol': 'usuario', 'texto': 'Hola'},
        ])
        self.assertEqual(len(contenidos), 1)
        self.assertEqual(contenidos[0]['role'], 'user')

    def test_recorta_los_mensajes_muy_largos(self):
        contenidos = servicio.a_contenidos([{'rol': 'usuario', 'texto': 'a' * 5000}])
        self.assertEqual(len(contenidos[0]['parts'][0]['text']), servicio.MAX_TEXTO)

    def test_solo_envia_los_ultimos_mensajes(self):
        mensajes = [{'rol': 'usuario' if i % 2 == 0 else 'asistente', 'texto': f'm{i}'} for i in range(30)]
        mensajes.append({'rol': 'usuario', 'texto': 'ultimo'})
        contenidos = servicio.a_contenidos(mensajes)
        self.assertLessEqual(len(contenidos), servicio.MAX_MENSAJES)
        self.assertEqual(contenidos[-1]['parts'][0]['text'], 'ultimo')


@override_settings(GEMINI_API_KEY='clave-de-prueba', GEMINI_MODEL='modelo-de-prueba')
class GeminiUnitTests(SimpleTestCase):
    def _llamar(self):
        return gemini.llamar('instruccion', [{'role': 'user', 'parts': [{'text': 'hola'}]}], [])

    @override_settings(GEMINI_API_KEY='')
    def test_sin_clave_da_error_503(self):
        with self.assertRaises(ErrorGemini) as contexto:
            self._llamar()
        self.assertEqual(contexto.exception.estado, 503)
        self.assertIn('GEMINI_API_KEY', contexto.exception.mensaje)

    @patch('asistente.gemini.urllib.request.urlopen')
    def test_envia_la_clave_en_la_cabecera_y_el_modelo_en_la_direccion(self, urlopen):
        urlopen.return_value.__enter__.return_value.read.return_value = json.dumps(texto('ok')).encode()
        self.assertEqual(self._llamar(), texto('ok'))
        peticion = urlopen.call_args.args[0]
        self.assertIn('modelo-de-prueba', peticion.full_url)
        self.assertEqual(peticion.get_header('X-goog-api-key'), 'clave-de-prueba')
        self.assertNotIn('clave-de-prueba', peticion.full_url)  # la clave no va en la dirección

    @patch('asistente.gemini.urllib.request.urlopen')
    def test_limite_de_cuota_da_un_mensaje_claro(self, urlopen):
        urlopen.side_effect = urllib.error.HTTPError('u', 429, 'Too Many Requests', {}, None)
        with self.assertRaises(ErrorGemini) as contexto:
            self._llamar()
        self.assertIn('límite', contexto.exception.mensaje)

    @patch('asistente.gemini.urllib.request.urlopen')
    def test_clave_rechazada_da_un_mensaje_claro(self, urlopen):
        urlopen.side_effect = urllib.error.HTTPError('u', 403, 'Forbidden', {}, None)
        with self.assertRaises(ErrorGemini) as contexto:
            self._llamar()
        self.assertIn('GEMINI_API_KEY', contexto.exception.mensaje)

    @patch('asistente.gemini.urllib.request.urlopen')
    def test_sin_internet_da_un_mensaje_claro(self, urlopen):
        urlopen.side_effect = urllib.error.URLError('sin red')
        with self.assertRaises(ErrorGemini) as contexto:
            self._llamar()
        self.assertIn('conectar', contexto.exception.mensaje)

    def test_las_herramientas_declaradas_existen(self):
        for declaracion in herramientas.DECLARACIONES:
            self.assertIn(declaracion['name'], herramientas.FUNCIONES)


# ====================== PRUEBAS DE INTEGRACIÓN ======================

class HerramientasTests(APITestCase):
    """Cada herramienta lee datos reales de la base."""

    def setUp(self):
        self.contexto = Contexto()
        self.admin = crear_admin()
        self.ana = crear_usuario('Ana Torres', 'ana@correo.com')
        self.luis = crear_usuario('Luis Ríos', 'luis@gmail.com', activo=False)

    def test_resumen_cuenta_usuarios_activos_e_inactivos_sin_administradores(self):
        crear_categoria('Saludos')
        crear_sena('Hola')
        datos = herramientas.resumen_panel(self.contexto)
        self.assertEqual((datos['usuarios']['total'], datos['usuarios']['activos'], datos['usuarios']['inactivos']),
                         (2, 1, 1))
        self.assertEqual((datos['categorias_total'], datos['senas_total']), (1, 1))

    def test_buscar_usuarios_dice_si_esta_activo(self):
        datos = herramientas.buscar_usuarios(self.contexto, texto='ana')
        self.assertEqual(datos['usuarios'], [
            {'id': self.ana.pk, 'nombre': 'Ana Torres', 'email': 'ana@correo.com', 'activo': True}])

    def test_buscar_usuarios_filtra_por_estado(self):
        datos = herramientas.buscar_usuarios(self.contexto, estado='inactivo')
        self.assertEqual([u['nombre'] for u in datos['usuarios']], ['Luis Ríos'])

    def test_buscar_usuarios_nunca_devuelve_contraseñas_ni_administradores(self):
        datos = herramientas.buscar_usuarios(self.contexto)
        self.assertEqual(datos['total_coincidencias'], 2)
        self.assertNotIn('password_hash', json.dumps(datos))

    def test_listar_categorias_cuenta_sus_señas(self):
        saludos = crear_categoria('Saludos')
        crear_categoria('Familia')
        crear_sena('Hola', saludos)
        crear_sena('Chau', saludos)
        datos = herramientas.listar_categorias(self.contexto)
        self.assertEqual(datos['total'], 2)
        self.assertIn({'nombre': 'Saludos', 'total_senas': 2}, datos['categorias'])
        self.assertIn({'nombre': 'Familia', 'total_senas': 0}, datos['categorias'])

    def test_estado_entrenamiento_separa_entrenadas_y_pendientes(self):
        lista = crear_sena('Lista')
        crear_sena('Pendiente')
        MuestraSena.objects.bulk_create([MuestraSena(sena=lista, puntos='0') for _ in range(META_MUESTRAS)])
        datos = herramientas.estado_entrenamiento(self.contexto)
        self.assertEqual((datos['entrenadas'], datos['por_entrenar']), (1, 1))
        self.assertEqual(datos['nombres_por_entrenar'], ['Pendiente'])

    def test_buscar_senas_por_categoria(self):
        saludos = crear_categoria('Saludos')
        familia = crear_categoria('Familia')
        crear_sena('Hola', saludos)
        crear_sena('Mamá', familia)
        datos = herramientas.buscar_senas(self.contexto, categoria='famil')
        self.assertEqual([s['nombre'] for s in datos['senas']], ['Mamá'])

    def test_top_traducciones_ordena_por_cantidad(self):
        for nombre in ('Hola', 'Hola', 'Gracias'):
            RegistroUso.objects.create(nombre_sena=nombre)
        datos = herramientas.top_traducciones(self.contexto, dias='todo')
        self.assertEqual(datos['total_traducciones'], 3)
        self.assertEqual(datos['mas_traducidas'][0]['nombre'], 'Hola')

    def test_top_traducciones_con_periodo_invalido_usa_30_dias(self):
        self.assertIn('30', herramientas.top_traducciones(self.contexto, dias='cualquiera')['periodo'])

    def test_cambiar_estado_solo_propone_no_cambia_nada(self):
        datos = herramientas.cambiar_estado_usuario(self.contexto, self.ana.pk, False)
        self.assertTrue(datos['pendiente_de_confirmacion'])
        self.assertEqual(self.contexto.accion,
                         {'tipo': 'cambiar_estado', 'id_usuario': self.ana.pk, 'nombre': 'Ana Torres',
                          'activar': False})
        self.ana.refresh_from_db()
        self.assertTrue(self.ana.activo)

    def test_cambiar_estado_a_un_usuario_que_ya_esta_asi_no_propone_nada(self):
        datos = herramientas.cambiar_estado_usuario(self.contexto, self.luis.pk, False)
        self.assertTrue(datos['sin_cambios'])
        self.assertIsNone(self.contexto.accion)

    def test_cambiar_estado_de_un_usuario_inexistente_da_error(self):
        self.assertIn('error', herramientas.cambiar_estado_usuario(self.contexto, 9999, True))
        self.assertIsNone(self.contexto.accion)

    def test_un_administrador_no_se_puede_desactivar(self):
        self.assertIn('error', herramientas.cambiar_estado_usuario(self.contexto, self.admin.pk, False))
        self.assertIsNone(self.contexto.accion)

    def test_herramienta_inexistente_devuelve_error(self):
        self.assertIn('error', herramientas.ejecutar('borrar_todo', {}, self.contexto))

    def test_argumentos_invalidos_devuelven_error(self):
        self.assertIn('error', herramientas.ejecutar('buscar_usuarios', {'inventado': 1}, self.contexto))
        self.assertIn('error', herramientas.ejecutar('cambiar_estado_usuario', {'id_usuario': 'x', 'activar': 1},
                                                     self.contexto))


class ConversacionTests(APITestCase):
    """El asistente pide herramientas a Gemini y arma la respuesta."""

    def setUp(self):
        self.ana = crear_usuario('Ana Torres', 'ana@correo.com')

    def _responder(self, pregunta, respuestas):
        with patch('asistente.servicio.gemini.llamar', side_effect=respuestas) as simulada:
            resultado = servicio.responder([{'rol': 'usuario', 'texto': pregunta}])
        return resultado, simulada

    def test_respuesta_directa(self):
        resultado, _ = self._responder('Hola', [texto('¡Hola! ¿En qué te ayudo?')])
        self.assertEqual(resultado, {'respuesta': '¡Hola! ¿En qué te ayudo?', 'accion': None})

    def test_usa_una_herramienta_y_le_devuelve_los_datos_reales_a_la_ia(self):
        resultado, simulada = self._responder(
            '¿Cuántos usuarios hay?', [llamada('resumen_panel'), texto('Hay 1 usuario.')])
        self.assertEqual(resultado['respuesta'], 'Hay 1 usuario.')
        self.assertEqual(simulada.call_count, 2)
        contenidos = simulada.call_args_list[1].args[1]
        respuesta_herramienta = contenidos[-1]['parts'][0]['functionResponse']
        self.assertEqual(respuesta_herramienta['name'], 'resumen_panel')
        self.assertEqual(respuesta_herramienta['response']['resultado']['usuarios']['total'], 1)

    def test_propone_una_accion_pero_no_la_ejecuta(self):
        resultado, _ = self._responder(
            'Desactiva a Ana',
            [llamada('cambiar_estado_usuario', {'id_usuario': self.ana.pk, 'activar': False}),
             texto('Queda pendiente de tu confirmación.')])
        self.assertEqual(resultado['accion']['id_usuario'], self.ana.pk)
        self.assertFalse(resultado['accion']['activar'])
        self.ana.refresh_from_db()
        self.assertTrue(self.ana.activo)

    def test_si_la_ia_pide_herramientas_sin_parar_se_corta(self):
        resultado, simulada = self._responder('Hola', [llamada('resumen_panel')] * 10)
        self.assertEqual(simulada.call_count, servicio.MAX_PASOS)
        self.assertIn('No logré', resultado['respuesta'])

    def test_respuesta_vacia_de_la_ia_da_un_mensaje_por_defecto(self):
        resultado, _ = self._responder('Hola', [{'candidates': []}])
        self.assertIn('No pude responder', resultado['respuesta'])

    def test_ignora_los_pensamientos_internos_de_la_ia(self):
        respuesta = {'candidates': [{'content': {'parts': [
            {'text': 'pensando...', 'thought': True}, {'text': 'Respuesta final'}]}}]}
        resultado, _ = self._responder('Hola', [respuesta])
        self.assertEqual(resultado['respuesta'], 'Respuesta final')


class AsistenteApiTests(APITestCase):
    """/api/asistente/ y /api/asistente/accion/"""

    def setUp(self):
        self.admin = crear_admin()
        self.cliente = cliente_de(self.admin)
        self.ana = crear_usuario('Ana Torres', 'ana@correo.com')
        self.luis = crear_usuario('Luis Ríos', 'luis@gmail.com', activo=False)

    def _preguntar(self, mensajes=None):
        mensajes = mensajes if mensajes is not None else [{'rol': 'usuario', 'texto': 'Hola'}]
        return self.cliente.post('/api/asistente/', {'mensajes': mensajes}, format='json')

    def test_sin_sesion_de_administrador_da_403(self):
        self.assertEqual(APIClient().post('/api/asistente/', {}, format='json').status_code, 403)
        self.assertEqual(APIClient().post('/api/asistente/accion/', {}, format='json').status_code, 403)

    def test_usuario_comun_no_puede_usar_el_asistente(self):
        r = cliente_de(self.ana).post('/api/asistente/', {'mensajes': []}, format='json')
        self.assertEqual(r.status_code, 403)

    @patch('asistente.servicio.gemini.llamar', return_value=texto('Hola, administrador.'))
    def test_responde_la_pregunta(self, _):
        r = self._preguntar()
        self.assertEqual(r.status_code, 200, r.content)
        self.assertEqual((r.data['exito'], r.data['respuesta'], r.data['accion']),
                         (True, 'Hola, administrador.', None))

    def test_mensajes_invalidos_dan_400(self):
        for mensajes in ([], [{'rol': 'usuario', 'texto': ''}], 'hola'):
            self.assertEqual(self._preguntar(mensajes).status_code, 400, mensajes)

    @override_settings(GEMINI_API_KEY='')
    def test_sin_clave_configurada_da_503_con_el_motivo(self):
        r = self._preguntar()
        self.assertEqual(r.status_code, 503)
        self.assertIn('GEMINI_API_KEY', r.data['error'])

    def test_un_fallo_de_gemini_se_informa_sin_romper(self):
        with patch('asistente.servicio.gemini.llamar', side_effect=ErrorGemini('Se alcanzó el límite.')):
            r = self._preguntar()
        self.assertEqual((r.status_code, r.data['exito']), (502, False))
        self.assertEqual(r.data['error'], 'Se alcanzó el límite.')

    # ---- confirmar una acción ----
    def _confirmar(self, id_usuario, activar):
        return self.cliente.post('/api/asistente/accion/', {'id_usuario': id_usuario, 'activar': activar},
                                 format='json')

    def test_confirmar_desactiva_la_cuenta(self):
        r = self._confirmar(self.ana.pk, False)
        self.assertEqual(r.status_code, 200, r.content)
        self.assertFalse(r.data['data']['is_active'])
        self.ana.refresh_from_db()
        self.assertFalse(self.ana.activo)

    def test_confirmar_activa_la_cuenta(self):
        self._confirmar(self.luis.pk, True)
        self.luis.refresh_from_db()
        self.assertTrue(self.luis.activo)

    def test_confirmar_un_usuario_inexistente_da_404(self):
        self.assertEqual(self._confirmar(9999, True).status_code, 404)

    def test_un_administrador_no_se_puede_desactivar_por_aqui(self):
        self.assertEqual(self._confirmar(self.admin.pk, False).status_code, 404)
        self.admin.refresh_from_db()
        self.assertTrue(self.admin.activo)

    def test_confirmar_con_datos_invalidos_da_400(self):
        self.assertEqual(self._confirmar('abc', True).status_code, 400)
        self.assertEqual(self._confirmar(self.ana.pk, 'quizas').status_code, 400)
        self.ana.refresh_from_db()
        self.assertTrue(self.ana.activo)
