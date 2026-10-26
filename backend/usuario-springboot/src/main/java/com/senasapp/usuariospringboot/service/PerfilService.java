package com.senasapp.usuariospringboot.service;

import com.senasapp.usuariospringboot.dto.FotoRequest;
import com.senasapp.usuariospringboot.dto.PasswordRequest;
import com.senasapp.usuariospringboot.dto.PerfilDatosRequest;
import com.senasapp.usuariospringboot.dto.UsuarioRespuesta;
import com.senasapp.usuariospringboot.model.Usuario;
import com.senasapp.usuariospringboot.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
public class PerfilService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository repositorio;
    private final PasswordEncoder cifrador;

    public PerfilService(UsuarioRepository repositorio, PasswordEncoder cifrador) {
        this.repositorio = repositorio;
        this.cifrador = cifrador;
    }

    // Cambia nombre y correo. 400 si no es válido, 409 si el correo es de otra persona.
    public UsuarioRespuesta actualizarDatos(Integer id, PerfilDatosRequest req) {
        Usuario u = buscar(id);
        String nombre = limpiar(req.nombre());
        String email = limpiar(req.email()).toLowerCase();

        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (nombre.length() > 50) throw new IllegalArgumentException("El nombre es demasiado largo (máximo 50 caracteres)");
        if (!EMAIL.matcher(email).matches()) throw new IllegalArgumentException("El correo no es válido");
        if (!email.equals(u.getEmail()) && repositorio.existsByEmail(email))
            throw new IllegalStateException("Ese correo ya está registrado");

        u.setNombre(nombre);
        u.setEmail(email);
        return aRespuesta(repositorio.save(u));
    }

    // Cambia la contraseña. Pide la actual: 401 si no coincide.
    public void cambiarPassword(Integer id, PasswordRequest req) {
        Usuario u = buscar(id);
        String actual = req.actual() == null ? "" : req.actual();
        String nueva = req.nueva() == null ? "" : req.nueva();

        if (!cifrador.matches(actual, u.getPasswordHash()))
            throw new SecurityException("La contraseña actual no es correcta");
        if (nueva.length() < 6) throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        if (nueva.length() > 30) throw new IllegalArgumentException("La contraseña es demasiado larga (máximo 30 caracteres)");
        if (cifrador.matches(nueva, u.getPasswordHash()))
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta a la actual");

        u.setPasswordHash(cifrador.encode(nueva));
        repositorio.save(u);
    }

    // Guarda la foto (data URL de imagen). Vacía o nula = quitar la foto.
    public UsuarioRespuesta actualizarFoto(Integer id, FotoRequest req) {
        Usuario u = buscar(id);
        String foto = req.foto();

        if (foto == null || foto.isBlank()) {
            u.setFoto(null);
        } else {
            if (!foto.startsWith("data:image/")) throw new IllegalArgumentException("La foto no es válida");
            if (foto.length() > 300_000) throw new IllegalArgumentException("La foto es demasiado grande");
            u.setFoto(foto);
        }
        return aRespuesta(repositorio.save(u));
    }

    private Usuario buscar(Integer id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private UsuarioRespuesta aRespuesta(Usuario u) {
        return new UsuarioRespuesta(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol(), u.getFoto());
    }
}