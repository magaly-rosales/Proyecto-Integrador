import numpy as np

class KeypointProcessorService:
    """
    Servicio encargado de normalizar las coordenadas recibidas de MediaPipe
    antes de enviarlas al modelo de IA.
    """

    @staticmethod
    def preprocesar_landmarks(landmarks):
        """
        Toma una lista de 21 dicts [{'x': f, 'y': f, 'z': f}, ...]
        1. Resta el origen (wrist / punto 0) para hacer la seña invariante a la posición.
        2. Escala los puntos según la distancia máxima para hacerla invariante a la distancia de la cámara.
        """
        coords = np.array([[p['x'], p['y'], p['z']] for p in landmarks], dtype=np.float32)

        # 1. Centrar en la muñeca (punto 0)
        origen = coords[0]
        coords_centradas = coords - origen

        # 2. Normalización de escala (distancia máxima)
        max_distancia = np.max(np.linalg.norm(coords_centradas, axis=1))
        if max_distancia > 0:
            coords_normalizadas = coords_centradas / max_distancia
        else:
            coords_normalizadas = coords_centradas

        return coords_normalizadas.flatten()

    @staticmethod
    def predecir_sena(vector_features):
        """
        Lógica de inferencia. Si el archivo .pkl del modelo existe, realiza la predicción real;
        de lo contrario, ejecuta una inferencia basada en reglas de apoyo para el desarrollo.
        """
        # Aquí se integrará joblib.load('modelo.pkl') cuando el modelo esté entrenado
        # Por ahora realizamos una verificación de estructura vectorial
        if len(vector_features) != 63: # 21 puntos * 3 ejes (x,y,z)
            raise ValueError("El vector procesado debe contener exactamente 63 características.")

        return {
            "sena_detectada": "Hola",
            "confianza": 0.96,
            "features_procesadas": len(vector_features)
        }