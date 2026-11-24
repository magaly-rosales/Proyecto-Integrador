"""Cuánto entrenamiento necesita una seña para considerarse 'Entrenada'."""

META_MUESTRAS = 2       # capturas de la mano quieta
META_MOVIMIENTOS = 2    # movimientos grabados


def esta_entrenada(muestras, movimientos):
    # Una seña quieta se entrena con muestras y una seña con movimiento con
    # grabaciones: con cualquiera de las dos metas ya queda entrenada.
    return muestras >= META_MUESTRAS or movimientos >= META_MOVIMIENTOS


def faltan(muestras, movimientos):
    """Cuánto falta para llegar a cada meta (0 si ya se alcanzó)."""
    return {
        'muestras': max(META_MUESTRAS - muestras, 0),
        'movimientos': max(META_MOVIMIENTOS - movimientos, 0),
    }