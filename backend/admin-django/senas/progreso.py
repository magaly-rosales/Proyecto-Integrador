"""Cuánto entrenamiento necesita una seña para considerarse 'Entrenada'."""

META_MUESTRAS = 20      # capturas de la mano quieta
META_MOVIMIENTOS = 10   # movimientos grabados


def esta_entrenada(muestras, movimientos):
    # Una seña quieta se entrena con muestras y una seña con movimiento con
    # grabaciones: con cualquiera de las dos metas ya queda entrenada.
    return muestras >= META_MUESTRAS or movimientos >= META_MOVIMIENTOS