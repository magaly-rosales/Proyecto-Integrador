"""Reglas de negocio de las categorías (separadas de las vistas)."""
from django.db import transaction

from senas.models import Sena


class CategoriaConSenas(Exception):
    """La categoría no se puede eliminar porque todavía tiene señas."""

    def __init__(self, total):
        self.total = total
        super().__init__(f'La categoría tiene {total} seña(s).')


def guardar_categoria(serializer):
    """Guarda la categoría y deja igual el texto antiguo de sus señas.

    Spring Boot (web de usuario) todavía lee sena.categoria como texto,
    por eso se actualiza junto con el nombre.
    """
    with transaction.atomic():
        categoria = serializer.save()
        Sena.objects.filter(categoria_ref=categoria).update(categoria=categoria.nombre)
    return categoria


def eliminar_categoria(categoria):
    total = categoria.senas.count()
    if total:
        raise CategoriaConSenas(total)
    categoria.delete()