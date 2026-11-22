from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    SenaViewSet, UserViewSet, RegistroUsoViewSet,
    predecir_sena, estadisticas_uso, login_admin,
    resumen_entrenamiento, borrar_entrenamiento,
    guardar_muestra, guardar_secuencia
)

router = DefaultRouter()
router.register(r'senas', SenaViewSet, basename='sena')
router.register(r'usuarios', UserViewSet, basename='usuario')
router.register(r'registro-uso', RegistroUsoViewSet, basename='registrouso')

urlpatterns = [
    path('', include(router.urls)),
    path('estadisticas/', estadisticas_uso, name='estadisticas_uso'),
    path('predecir/', predecir_sena, name='predecir_sena'),
    path('login/', login_admin, name='login_admin'),
    path('entrenamiento/', resumen_entrenamiento, name='resumen_entrenamiento'),
    path('entrenamiento/<int:id_sena>/', borrar_entrenamiento, name='borrar_entrenamiento'),
    path('entrenamiento/muestras/<int:id_sena>/', guardar_muestra, name='guardar_muestra'),
    path('entrenamiento/secuencias/<int:id_sena>/', guardar_secuencia, name='guardar_secuencia'),
]