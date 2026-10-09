package com.senasapp.usuariospringboot.controller;

import com.senasapp.usuariospringboot.dto.PuntoMano;
import com.senasapp.usuariospringboot.model.Sena;
import com.senasapp.usuariospringboot.repository.SenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reconocimiento")
public class ReconocimientoController {

    @Autowired
    private SenaRepository senaRepository;

    // Recibe los 21 puntos de la mano (Jeff conecta aquí el clasificador)
    @PostMapping("/puntos")
    public Map<String, Object> recibirPuntos(@RequestBody List<PuntoMano> puntos) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "recibido");
        respuesta.put("cantidad_puntos", puntos.size());
        respuesta.put("mensaje", "Puntos recibidos, pendiente de clasificar");
        return respuesta;
    }

    // Prueba de conexión a la tabla sena
    @GetMapping("/senas-disponibles")
    public List<Sena> listarSenas() {
        return senaRepository.findAll();
    }
}