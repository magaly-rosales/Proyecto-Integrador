package com.senasapp.usuariospringboot.controller;

import com.senasapp.usuariospringboot.dto.PuntoMano;
import com.senasapp.usuariospringboot.model.Sena;
import com.senasapp.usuariospringboot.repository.SenaRepository;
import com.senasapp.usuariospringboot.service.ClasificadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.senasapp.usuariospringboot.service.SecuenciaService;
import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/reconocimiento")
public class ReconocimientoController {

    @Autowired
    private SenaRepository senaRepository;

    @Autowired
    private ClasificadorService clasificadorService;
    @Autowired
    private SecuenciaService secuenciaService;

    private String validarSecuencia(List<List<PuntoMano>> cuadros) {
        if (cuadros == null || cuadros.size() < 8 || cuadros.size() > 80)
            return "Se esperan entre 8 y 80 cuadros";
        int tam = cuadros.get(0).size();
        if (tam != 21 && tam != 42) return "Cada cuadro debe tener 21 o 42 puntos";
        for (List<PuntoMano> c : cuadros)
            if (c.size() != tam) return "Todos los cuadros deben tener el mismo número de puntos";
        return null;
    }

    // Reconoce una seña con movimiento
    @PostMapping("/secuencia")
    public ResponseEntity<?> reconocerSecuencia(@RequestBody List<List<PuntoMano>> cuadros) {
        String error = validarSecuencia(cuadros);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        return ResponseEntity.ok(secuenciaService.clasificar(cuadros));
    }

    // Guarda un ejemplo de seña con movimiento
    @PostMapping("/secuencias/{idSena}")
    public ResponseEntity<?> guardarSecuencia(@PathVariable Integer idSena,
                                              @RequestBody List<List<PuntoMano>> cuadros) {
        String error = validarSecuencia(cuadros);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        if (!senaRepository.existsById(idSena))
            return ResponseEntity.badRequest().body(Map.of("error", "La seña no existe"));
        secuenciaService.guardar(idSena, cuadros);
        return ResponseEntity.ok(Map.of("status", "secuencia guardada"));
    }

    // Recibe los 21 o 42 puntos y devuelve la seña reconocida
    @PostMapping("/puntos")
    public ResponseEntity<?> recibirPuntos(@RequestBody List<PuntoMano> puntos) {
        if (puntos.size() != 21 && puntos.size() != 42) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se esperan 21 puntos (una mano) o 42 (dos manos)"));
        }
        return ResponseEntity.ok(clasificadorService.clasificar(puntos));

    }

    // Guarda un ejemplo de una seña (para "entrenar")
    @PostMapping("/muestras/{idSena}")
    public ResponseEntity<?> guardarMuestra(@PathVariable Integer idSena,
                                            @RequestBody List<PuntoMano> puntos) {
        if (puntos.size() != 21 && puntos.size() != 42) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se esperan 21 puntos (una mano) o 42 (dos manos)"));
        }
        clasificadorService.guardarMuestra(idSena, puntos);
        return ResponseEntity.ok(Map.of("status", "muestra guardada"));
    }

    @GetMapping("/senas-disponibles")
    public List<Sena> listarSenas() {
        return senaRepository.findAll();
    }
}