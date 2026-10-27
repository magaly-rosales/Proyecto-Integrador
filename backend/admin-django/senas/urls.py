from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    SenaViewSet,
    UserViewSet,
    RegistroUsoViewSet,
    predecir_sena,
    estadisticas_uso
)

router = DefaultRouter()
router.register(r'senas', SenaViewSet)
router.register(r'usuarios', UserViewSet, basename='usuario')
router.register(r'registro-uso', RegistroUsoViewSet, basename='registrouso')

urlpatterns = [
    path('', include(router.urls)),
    path('estadisticas/', estadisticas_uso, name='estadisticas_uso'),
    path('predecir/', predecir_sena, name='predecir_sena'),
]