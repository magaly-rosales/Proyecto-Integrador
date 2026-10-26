package com.senasapp.usuariospringboot.controller;

import com.senasapp.usuariospringboot.dto.PuntoMano;
import com.senasapp.usuariospringboot.model.Sena;
import com.senasapp.usuariospringboot.repository.SenaRepository;
import com.senasapp.usuariospringboot.service.ClasificadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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