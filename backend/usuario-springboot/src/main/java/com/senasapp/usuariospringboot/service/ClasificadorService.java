package com.senasapp.usuariospringboot.service;

import com.senasapp.usuariospringboot.dto.PuntoMano;
import com.senasapp.usuariospringboot.model.MuestraSena;
import com.senasapp.usuariospringboot.model.Sena;
import com.senasapp.usuariospringboot.repository.MuestraSenaRepository;
import com.senasapp.usuariospringboot.repository.SenaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClasificadorService {

    private static final int K = 3;
    private static final double UMBRAL = 0.8; // valor inicial, ajústalo probando

    private final MuestraSenaRepository muestraRepository;
    private final SenaRepository senaRepository;

    public ClasificadorService(MuestraSenaRepository muestraRepository, SenaRepository senaRepository) {
        this.muestraRepository = muestraRepository;
        this.senaRepository = senaRepository;
    }

    // 21 puntos (una mano) o 42 (dos manos) -> relativos al primer punto y escalados
    private double[] normalizar(List<PuntoMano> p) {
        int n = p.size();
        PuntoMano origen = p.get(0);
        double[] v = new double[n * 3];
        for (int i = 0; i < n; i++) {
            v[i * 3] = p.get(i).getX() - origen.getX();
            v[i * 3 + 1] = p.get(i).getY() - origen.getY();
            v[i * 3 + 2] = p.get(i).getZ() - origen.getZ();
        }
        double max = 0;
        for (double d : v) max = Math.max(max, Math.abs(d));
        if (max > 0) for (int i = 0; i < v.length; i++) v[i] /= max;
        return v;
    }

    public void guardarMuestra(Integer idSena, List<PuntoMano> puntos) {
        Sena sena = senaRepository.findById(idSena)
                .orElseThrow(() -> new IllegalArgumentException("La seña no existe"));
        MuestraSena m = new MuestraSena();
        m.setSena(sena);
        m.setPuntos(Arrays.stream(normalizar(puntos))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")));
        muestraRepository.save(m);
    }



    public Map<String, Object> clasificar(List<PuntoMano> puntos) {
        Map<String, Object> res = new HashMap<>();
        List<MuestraSena> muestras = muestraRepository.findAll();
        if (muestras.isEmpty()) {
            res.put("sena", "sin_muestras");
            return res;
        }
        double[] v = normalizar(puntos);

        record Vecino(double distancia, String nombre) {}
        List<Vecino> vecinos = new ArrayList<>();

        // ---- BLOQUE NUEVO (reemplaza el for y el sort anteriores) ----
        for (MuestraSena m : muestras) {
            double[] ref = Arrays.stream(m.getPuntos().split(","))
                    .mapToDouble(Double::parseDouble).toArray();
            if (ref.length != v.length) continue; // solo compara 1 mano con 1 mano, 2 con 2
            vecinos.add(new Vecino(distancia(v, ref), m.getSena().getNombre()));
        }
        if (vecinos.isEmpty()) {
            res.put("sena", "sin_muestras");
            return res;
        }
        vecinos.sort(Comparator.comparingDouble(Vecino::distancia));
        // ---- FIN DEL BLOQUE NUEVO ----

        List<Vecino> top = vecinos.subList(0, Math.min(K, vecinos.size()));
        String ganador = top.stream()
                .collect(Collectors.groupingBy(Vecino::nombre, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();

        double mejor = top.get(0).distancia();
        res.put("sena", mejor > UMBRAL ? "desconocida" : ganador);
        res.put("distancia", mejor);
        return res;
    }

    private double distancia(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) s += (a[i] - b[i]) * (a[i] - b[i]);
        return Math.sqrt(s);
    }
}