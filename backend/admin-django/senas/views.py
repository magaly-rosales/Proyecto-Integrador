from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from rest_framework.decorators import api_view
from django.contrib.auth.models import User
from .models import Sena
from .serializers import SenaSerializer, UserSerializer
from .services import KeypointProcessorService

class SenaViewSet(viewsets.ModelViewSet):
    queryset = Sena.objects.all().order_by('-fecha_creacion')
    serializer_class = SenaSerializer
    parser_classes = (MultiPartParser, FormParser, JSONParser)

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

    def update(self, request, *args, **kwargs):
        partial = kwargs.pop('partial', False)
        instance = self.get_object()
        serializer = self.get_serializer(instance, data=request.data, partial=partial)

        if not serializer.is_valid():
            return Response({
                "exito": False,
                "errores": serializer.errors
            }, status=status.HTTP_400_BAD_REQUEST)

        self.perform_update(serializer)
        return Response({
            "exito": True,
            "mensaje": "Seña actualizada correctamente.",
            "data": serializer.data
        }, status=status.HTTP_200_OK)

    def destroy(self, request, *args, **kwargs):
        instance = self.get_object()
        self.perform_destroy(instance)
        return Response({
            "exito": True,
            "mensaje": "Seña eliminada correctamente del sistema."
        }, status=status.HTTP_200_OK)


class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all().order_by('-date_joined')
    serializer_class = UserSerializer

    def partial_update(self, request, *args, **kwargs):
        instance = self.get_object()
        is_active = request.data.get('is_active', None)
        if is_active is not None:
            instance.is_active = is_active
            instance.save()
            return Response({
                "exito": True,
                "mensaje": f"Estado del usuario {'activado' if is_active else 'desactivado'} correctamente.",
                "data": self.get_serializer(instance).data
            }, status=status.HTTP_200_OK)
        return super().partial_update(request, *args, **kwargs)


@api_view(['POST'])
def predecir_sena(request):
    landmarks = request.data.get('landmarks', [])

    if not isinstance(landmarks, list) or len(landmarks) != 21:
        return Response({
            "exito": False,
            "error": "Se requieren exactamente 21 puntos (keypoints) con coordenadas (x, y, z)."
        }, status=status.HTTP_400_BAD_REQUEST)

    try:
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