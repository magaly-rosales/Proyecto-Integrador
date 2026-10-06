package com.senasapp.usuariospringboot;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reconocimiento")
public class ReconocimientoController {

    @PostMapping("/imagen")
    public Map<String, String> recibirImagen(@RequestParam("imagen") MultipartFile imagen) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "recibido");
        respuesta.put("mensaje", "Imagen recibida correctamente: " + imagen.getOriginalFilename());
        return respuesta;
    }
}