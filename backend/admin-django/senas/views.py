from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.decorators import api_view
from .models import Sena
from .serializers import SenaSerializer
from .services import KeypointProcessorService

class SenaViewSet(viewsets.ModelViewSet):
    queryset = Sena.objects.all()
    serializer_class = SenaSerializer
    parser_classes = (MultiPartParser, FormParser)

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            return Response({
                "exito": False,
                "errores": serializer.errors
            }, status=status.HTTP_400_BAD_REQUEST)

        self.perform_create(serializer)
        return Response({
            "exito": True,
            "mensaje": "Seña registrada correctamente en el sistema.",
            "data": serializer.data
        }, status=status.HTTP_201_CREATED)


@api_view(['POST'])
def predecir_sena(request):
    landmarks = request.data.get('landmarks', [])

    if not isinstance(landmarks, list) or len(landmarks) != 21:
        return Response({
            "exito": False,
            "error": "Se requieren exactamente 21 puntos (keypoints) con coordenadas (x, y, z)."
        }, status=status.HTTP_400_BAD_REQUEST)

    try:
        # Preprocesamiento vectorial real
        vector_features = KeypointProcessorService.preprocesar_landmarks(landmarks)
        resultado = KeypointProcessorService.predecir_sena(vector_features)

        return Response({
            "exito": True,
            "data": resultado
        }, status=status.HTTP_200_OK)

    except Exception as e:
        return Response({
            "exito": False,
            "error": str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)