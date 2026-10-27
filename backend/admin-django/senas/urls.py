from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SenaViewSet

router = DefaultRouter()
router.register(r'senas', SenaViewSet)

urlpatterns = [
    path('', include(router.urls)),
]