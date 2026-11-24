"""Ejecutor de pruebas del proyecto.

Las tablas del proyecto son 'managed = False' (las crea el script SQL de /database),
así que Django, por defecto, no las crea en la base de datos de pruebas.
Este ejecutor, solo durante las pruebas:
  1. convierte esos modelos en administrados (managed = True), y
  2. ignora las migraciones (que también los declaran como no administrados),
para que Django cree las tablas desde los modelos en una base temporal (test_<nombre>).
La base real (senasapp_db) no se toca.
"""
from django.apps import apps
from django.conf import settings
from django.test.runner import DiscoverRunner


class _SinMigraciones:
    def __contains__(self, item):
        return True

    def __getitem__(self, item):
        return None


class PruebasRunner(DiscoverRunner):
    def setup_test_environment(self, **kwargs):
        super().setup_test_environment(**kwargs)
        self._modelos_no_administrados = [m for m in apps.get_models() if not m._meta.managed]
        for modelo in self._modelos_no_administrados:
            modelo._meta.managed = True
        self._migraciones_originales = getattr(settings, 'MIGRATION_MODULES', {})
        settings.MIGRATION_MODULES = _SinMigraciones()

    def teardown_test_environment(self, **kwargs):
        for modelo in self._modelos_no_administrados:
            modelo._meta.managed = False
        settings.MIGRATION_MODULES = self._migraciones_originales
        super().teardown_test_environment(**kwargs)
