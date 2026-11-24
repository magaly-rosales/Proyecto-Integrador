package com.senasapp.usuariospringboot.controller;

import com.senasapp.usuariospringboot.model.RegistroTraduccion;
import com.senasapp.usuariospringboot.repository.RegistroTraduccionRepository;
import com.senasapp.usuariospringboot.repository.SenaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/traducciones")
public class RegistroTraduccionController {

    private final RegistroTraduccionRepository registroRepository;
    private final SenaRepository senaRepository;

    public RegistroTraduccionController(RegistroTraduccionRepository registroRepository,
                                        SenaRepository senaRepository) {
        this.registroRepository = registroRepository;
        this.senaRepository = senaRepository;
    }

    // Anota una palabra traducida (para las estadísticas del admin)
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("sena", "").trim();
        if (nombre.isEmpty() || nombre.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre de seña no válido"));
        }
        RegistroTraduccion registro = new RegistroTraduccion();
        registro.setNombreSena(nombre);
        senaRepository.findFirstByNombreIgnoreCase(nombre)
                .ifPresent(s -> registro.setIdSena(s.getIdSena()));
        registroRepository.save(registro);
        return ResponseEntity.ok(Map.of("status", "registrado"));
    }
}