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

@RestController
@RequestMapping("/api/reconocimiento")
public class ReconocimientoController {

    @Autowired
    private SenaRepository senaRepository;

    @Autowired
    private ClasificadorService clasificadorService;

    // Recibe los 21 puntos y devuelve la seña reconocida
    @PostMapping("/puntos")
    public ResponseEntity<?> recibirPuntos(@RequestBody List<PuntoMano> puntos) {
        if (puntos.size() != 21) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se esperan 21 puntos"));
        }
        return ResponseEntity.ok(clasificadorService.clasificar(puntos));
    }

    // Guarda un ejemplo de una seña (para "entrenar")
    @PostMapping("/muestras/{idSena}")
    public ResponseEntity<?> guardarMuestra(@PathVariable Integer idSena,
                                            @RequestBody List<PuntoMano> puntos) {
        if (puntos.size() != 21) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se esperan 21 puntos"));
        }
        clasificadorService.guardarMuestra(idSena, puntos);
        return ResponseEntity.ok(Map.of("status", "muestra guardada"));
    }

    @GetMapping("/senas-disponibles")
    public List<Sena> listarSenas() {
        return senaRepository.findAll();
    }
}