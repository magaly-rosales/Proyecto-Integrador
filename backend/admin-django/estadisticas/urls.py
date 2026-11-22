from django.urls import path

from .views import estadisticas_uso, resumen

urlpatterns = [
    path('estadisticas/', estadisticas_uso, name='estadisticas_uso'),
    path('resumen/', resumen, name='resumen'),
]