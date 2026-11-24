from django.urls import path

from .views import confirmar_accion, preguntar

urlpatterns = [
    path('asistente/', preguntar, name='asistente'),
    path('asistente/accion/', confirmar_accion, name='asistente_accion'),
]
