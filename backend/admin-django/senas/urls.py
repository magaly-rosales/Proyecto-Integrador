from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SenaViewSet, predecir_sena

router = DefaultRouter()
router.register(r'senas', SenaViewSet)

urlpatterns = [
    path('', include(router.urls)),
    path('predecir/', predecir_sena, name='predecir_sena'),
]