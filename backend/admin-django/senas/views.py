from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser
from .models import Sena
from .serializers import SenaSerializer

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