from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SenaViewSet, UserViewSet, predecir_sena

router = DefaultRouter()
router.register(r'senas', SenaViewSet)
router.register(r'usuarios', UserViewSet, basename='usuario')

urlpatterns = [
    path('', include(router.urls)),
    path('predecir/', predecir_sena, name='predecir_sena'),
]