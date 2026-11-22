from django.db.models import Count
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from senas.models import Categoria

from .serializers import CategoriaSerializer, SenaDeCategoriaSerializer
from .services import CategoriaConSenas, eliminar_categoria, guardar_categoria


class CategoriaViewSet(viewsets.ModelViewSet):
    serializer_class = CategoriaSerializer

    def get_queryset(self):
        return Categoria.objects.annotate(_total=Count('senas')).order_by('nombre')

    def _error(self, errores):
        return Response({'exito': False, 'errores': errores}, status=status.HTTP_400_BAD_REQUEST)

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            return self._error(serializer.errors)
        categoria = guardar_categoria(serializer)
        return Response({
            'exito': True,
            'mensaje': 'Categoría creada correctamente.',
            'data': self.get_serializer(categoria).data,
        }, status=status.HTTP_201_CREATED)

    def update(self, request, *args, **kwargs):
        partial = kwargs.pop('partial', False)
        serializer = self.get_serializer(self.get_object(), data=request.data, partial=partial)
        if not serializer.is_valid():
            return self._error(serializer.errors)
        categoria = guardar_categoria(serializer)
        return Response({
            'exito': True,
            'mensaje': 'Categoría actualizada correctamente.',
            'data': self.get_serializer(categoria).data,
        })

    def destroy(self, request, *args, **kwargs):
        try:
            eliminar_categoria(self.get_object())
        except CategoriaConSenas as e:
            return self._error({'categoria': [
                f'No se puede eliminar: tiene {e.total} seña(s). Cámbialas de categoría primero.'
            ]})
        return Response({'exito': True, 'mensaje': 'Categoría eliminada correctamente.'})

    @action(detail=True, methods=['get'])
    def senas(self, request, pk=None):
        """GET /api/categorias/<id>/senas/ -> señas de esa categoría."""
        categoria = self.get_object()
        senas = categoria.senas.order_by('nombre')
        return Response(SenaDeCategoriaSerializer(senas, many=True).data)