import math
import numpy as np

N_CUADROS = 20  # igual que en SecuenciaService (Spring)


def _a_texto(valores):
    return ','.join(repr(float(x)) for x in valores)


def puntos_a_texto(puntos):
    """Muestra de una seña estática: puntos relativos al primero y escalados (como ClasificadorService)."""
    coords = np.array([[p['x'], p['y'], p['z']] for p in puntos], dtype=float)
    v = (coords - coords[0]).flatten()
    maximo = np.max(np.abs(v))
    if maximo > 0:
        v = v / maximo
    return _a_texto(v)


def secuencia_a_texto(cuadros):
    """Seña con movimiento: forma de la mano + recorrido de la muñeca, llevado a 20 cuadros (como SecuenciaService)."""
    manos = len(cuadros[0]) // 21
    m = len(cuadros)
    f = np.zeros((m, manos * 65))
    x0, y0, esc0 = [0.0] * manos, [0.0] * manos, [1.0] * manos

    for t, c in enumerate(cuadros):
        for h in range(manos):
            mu = c[h * 21]
            ref = c[h * 21 + 9]
            esc = max(math.hypot(ref['x'] - mu['x'], ref['y'] - mu['y']), 1e-6)
            if t == 0:
                x0[h], y0[h], esc0[h] = mu['x'], mu['y'], esc
            o = h * 65
            for i in range(21):
                p = c[h * 21 + i]
                f[t, o + i * 3] = (p['x'] - mu['x']) / esc
                f[t, o + i * 3 + 1] = (p['y'] - mu['y']) / esc
                f[t, o + i * 3 + 2] = (p['z'] - mu['z']) / esc
            f[t, o + 63] = (mu['x'] - x0[h]) / esc0[h]
            f[t, o + 64] = (mu['y'] - y0[h]) / esc0[h]

    r = np.zeros((N_CUADROS, f.shape[1]))
    for i in range(N_CUADROS):
        pos = i * (m - 1.0) / (N_CUADROS - 1)
        a = int(math.floor(pos))
        b = min(a + 1, m - 1)
        w = pos - a
        r[i] = f[a] * (1 - w) + f[b] * w

    return ';'.join(_a_texto(fila) for fila in r)