from django.urls import include, path
from rest_framework.routers import DefaultRouter

from .views import UsuarioViewSet, perfil, perfil_foto, perfil_password

router = DefaultRouter()
router.register(r'usuarios', UsuarioViewSet, basename='usuario')

urlpatterns = [
    path('perfil/', perfil, name='perfil'),
    path('perfil/foto/', perfil_foto, name='perfil_foto'),
    path('perfil/password/', perfil_password, name='perfil_password'),
    path('', include(router.urls)),
]
