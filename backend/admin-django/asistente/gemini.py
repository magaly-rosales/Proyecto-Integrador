"""Conexión con la API de Gemini (Google AI Studio). Usa solo la librería estándar."""
import json
import urllib.error
import urllib.request

from django.conf import settings

URL = 'https://generativelanguage.googleapis.com/v1beta/models/{modelo}:generateContent'
TIMEOUT_S = 30


class ErrorGemini(Exception):
    """Fallo al consultar a Gemini; trae el mensaje para el administrador y el código HTTP a devolver."""

    def __init__(self, mensaje, estado=502):
        super().__init__(mensaje)
        self.mensaje = mensaje
        self.estado = estado


def llamar(instruccion, contenidos, declaraciones):
    """Envía la conversación a Gemini y devuelve su respuesta (diccionario)."""
    clave = settings.GEMINI_API_KEY
    if not clave:
        raise ErrorGemini('El asistente no está configurado: falta GEMINI_API_KEY en el archivo .env.', 503)

    cuerpo = {
        'systemInstruction': {'parts': [{'text': instruccion}]},
        'contents': contenidos,
        'tools': [{'functionDeclarations': declaraciones}],
        'generationConfig': {'temperature': 0.2},
    }
    peticion = urllib.request.Request(
        URL.format(modelo=settings.GEMINI_MODEL),
        data=json.dumps(cuerpo).encode('utf-8'),
        headers={'Content-Type': 'application/json', 'x-goog-api-key': clave},
        method='POST',
    )
    try:
        with urllib.request.urlopen(peticion, timeout=TIMEOUT_S) as respuesta:
            return json.loads(respuesta.read().decode('utf-8'))
    except urllib.error.HTTPError as error:
        if error.code == 429:
            raise ErrorGemini('Se alcanzó el límite gratuito de Gemini. Espera un minuto e inténtalo de nuevo.')
        if error.code in (400, 403):
            raise ErrorGemini('Gemini rechazó la consulta: revisa la clave GEMINI_API_KEY y el modelo GEMINI_MODEL.')
        print(f'[asistente] Gemini respondió HTTP {error.code}: {error.read().decode("utf-8", "ignore")[:300]}')
        raise ErrorGemini('No se pudo consultar a Gemini en este momento.')
    except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as error:
        # El motivo real sale en la consola de Django para poder corregirlo
        print(f'[asistente] Fallo al conectar con Gemini: {type(error).__name__}: {error}')
        raise ErrorGemini('No se pudo conectar con Gemini. Revisa tu conexión a internet.')
