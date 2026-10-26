package com.senasapp.usuariospringboot.controller;

import com.senasapp.usuariospringboot.dto.FotoRequest;
import com.senasapp.usuariospringboot.dto.PasswordRequest;
import com.senasapp.usuariospringboot.dto.PerfilDatosRequest;
import com.senasapp.usuariospringboot.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService servicio;

    public PerfilController(PerfilService servicio) {
        this.servicio = servicio;
    }

    @PutMapping("/{id}/datos")
    public ResponseEntity<?> datos(@PathVariable Integer id, @RequestBody PerfilDatosRequest req) {
        return ejecutar(() -> servicio.actualizarDatos(id, req));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> password(@PathVariable Integer id, @RequestBody PasswordRequest req) {
        return ejecutar(() -> {
            servicio.cambiarPassword(id, req);
            return Map.of("mensaje", "Contraseña actualizada");
        });
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<?> foto(@PathVariable Integer id, @RequestBody FotoRequest req) {
        return ejecutar(() -> servicio.actualizarFoto(id, req));
    }

    // Convierte los errores del servicio en respuestas HTTP con { "error": "mensaje" }
    private ResponseEntity<?> ejecutar(Supplier<Object> accion) {
        try {
            return ResponseEntity.ok(accion.get());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e);
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e);
        } catch (SecurityException e) {
            return error(HttpStatus.UNAUTHORIZED, e);
        } catch (NoSuchElementException e) {
            return error(HttpStatus.NOT_FOUND, e);
        }
    }

    private ResponseEntity<?> error(HttpStatus estado, RuntimeException e) {
        return ResponseEntity.status(estado).body(Map.of("error", e.getMessage()));
    }
}