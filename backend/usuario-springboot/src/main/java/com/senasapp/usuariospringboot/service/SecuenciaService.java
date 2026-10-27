package com.senasapp.usuariospringboot.service;

import com.senasapp.usuariospringboot.dto.PuntoMano;
import com.senasapp.usuariospringboot.model.MuestraSecuencia;
import com.senasapp.usuariospringboot.model.Sena;
import com.senasapp.usuariospringboot.repository.MuestraSecuenciaRepository;
import com.senasapp.usuariospringboot.repository.SenaRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecuenciaService {

    private static final int N = 20;          // todas las secuencias se llevan a 20 cuadros
    private static final int BANDA = 6;       // cuánto se permite "estirar" en DTW
    private static final double UMBRAL = 2.0; // más lejos que esto = desconocida
    private static final double MARGEN = 0.8; // 1.º debe ser < 80 % de la distancia del 2.º

    private final MuestraSecuenciaRepository repo;
    private final SenaRepository senaRepo;

    public SecuenciaService(MuestraSecuenciaRepository repo, SenaRepository senaRepo) {
        this.repo = repo;
        this.senaRepo = senaRepo;
    }

    public void guardar(Integer idSena, List<List<PuntoMano>> cuadros) {
        Sena sena = senaRepo.findById(idSena)
                .orElseThrow(() -> new IllegalArgumentException("La seña no existe"));
        MuestraSecuencia m = new MuestraSecuencia();
        m.setSena(sena);
        m.setCuadros(serializar(caracteristicas(cuadros)));
        m.setNumCuadros(cuadros.size());
        repo.save(m);
    }

    public Map<String, Object> clasificar(List<List<PuntoMano>> cuadros) {
        double[][] entrada = caracteristicas(cuadros);
        int dim = entrada[0].length;

        Map<String, Double> mejorPorSena = new HashMap<>();
        for (MuestraSecuencia m : repo.findAll()) {
            double[][] guardada = deserializar(m.getCuadros());
            if (guardada[0].length != dim) continue; // distinto número de manos
            double d = dtw(entrada, guardada);
            mejorPorSena.merge(m.getSena().getNombre(), d, Math::min);
        }

        Map<String, Object> res = new HashMap<>();
        if (mejorPorSena.isEmpty()) {
            res.put("sena", "sin_muestras");
            res.put("distancia", 0.0);
            return res;
        }

        List<Map.Entry<String, Double>> orden = new ArrayList<>(mejorPorSena.entrySet());
        orden.sort(Map.Entry.comparingByValue());
        String ganador = orden.get(0).getKey();
        double dGanador = orden.get(0).getValue();
        double dOtra = orden.size() > 1 ? orden.get(1).getValue() : Double.MAX_VALUE;

        String resultado;
        if (dGanador > UMBRAL) resultado = "desconocida";
        else if (dGanador > MARGEN * dOtra) resultado = "dudosa";
        else resultado = ganador;

        res.put("sena", resultado);
        res.put("distancia", dGanador);
        return res;
    }

    // ---- Características: forma de la mano + recorrido de la muñeca ----
    private double[][] caracteristicas(List<List<PuntoMano>> cuadros) {
        int manos = cuadros.get(0).size() / 21;
        int m = cuadros.size();
        double[][] f = new double[m][manos * 65];
        double[] x0 = new double[manos], y0 = new double[manos], esc0 = new double[manos];

        for (int t = 0; t < m; t++) {
            List<PuntoMano> c = cuadros.get(t);
            for (int h = 0; h < manos; h++) {
                PuntoMano muneca = c.get(h * 21);
                PuntoMano ref = c.get(h * 21 + 9);
                double esc = Math.hypot(ref.getX() - muneca.getX(), ref.getY() - muneca.getY());
                if (esc < 1e-6) esc = 1e-6;
                if (t == 0) { x0[h] = muneca.getX(); y0[h] = muneca.getY(); esc0[h] = esc; }

                int o = h * 65;
                for (int i = 0; i < 21; i++) {
                    PuntoMano p = c.get(h * 21 + i);
                    f[t][o + i * 3] = (p.getX() - muneca.getX()) / esc;
                    f[t][o + i * 3 + 1] = (p.getY() - muneca.getY()) / esc;
                    f[t][o + i * 3 + 2] = (p.getZ() - muneca.getZ()) / esc;
                }
                f[t][o + 63] = (muneca.getX() - x0[h]) / esc0[h];
                f[t][o + 64] = (muneca.getY() - y0[h]) / esc0[h];
            }
        }
        return remuestrear(f);
    }

    // Lleva cualquier secuencia a N cuadros (así importa el recorrido, no cuántos cuadros hubo)
    private double[][] remuestrear(double[][] f) {
        int m = f.length, d = f[0].length;
        double[][] r = new double[N][d];
        for (int i = 0; i < N; i++) {
            double pos = i * (m - 1.0) / (N - 1);
            int a = (int) Math.floor(pos);
            int b = Math.min(a + 1, m - 1);
            double w = pos - a;
            for (int k = 0; k < d; k++) r[i][k] = f[a][k] * (1 - w) + f[b][k] * w;
        }
        return r;
    }

    // ---- DTW con banda ----
    private double dtw(double[][] a, double[][] b) {
        int n = a.length;
        double[][] D = new double[n + 1][n + 1];
        for (double[] fila : D) Arrays.fill(fila, Double.MAX_VALUE);
        D[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = Math.max(1, i - BANDA); j <= Math.min(n, i + BANDA); j++) {
                double c = distancia(a[i - 1], b[j - 1]);
                double previo = Math.min(D[i - 1][j], Math.min(D[i][j - 1], D[i - 1][j - 1]));
                D[i][j] = c + previo;
            }
        }
        return D[n][n] / n;
    }

    private double distancia(double[] u, double[] v) {
        double s = 0;
        for (int k = 0; k < u.length; k++) { double d = u[k] - v[k]; s += d * d; }
        return Math.sqrt(s);
    }

    // ---- Guardar como texto: cuadros separados por ";" y valores por "," ----
    private String serializar(double[][] seq) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seq.length; i++) {
            if (i > 0) sb.append(';');
            for (int k = 0; k < seq[i].length; k++) {
                if (k > 0) sb.append(',');
                sb.append(seq[i][k]);
            }
        }
        return sb.toString();
    }

    private double[][] deserializar(String texto) {
        String[] cuadros = texto.split(";");
        double[][] seq = new double[cuadros.length][];
        for (int i = 0; i < cuadros.length; i++) {
            String[] v = cuadros[i].split(",");
            seq[i] = new double[v.length];
            for (int k = 0; k < v.length; k++) seq[i][k] = Double.parseDouble(v[k]);
        }
        return seq;
    }
}