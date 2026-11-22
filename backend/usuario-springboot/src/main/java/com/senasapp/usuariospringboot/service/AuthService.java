package com.senasapp.usuariospringboot.service;

import com.senasapp.usuariospringboot.dto.LoginRequest;
import com.senasapp.usuariospringboot.dto.LoginRespuesta;
import com.senasapp.usuariospringboot.dto.RegistroRequest;
import com.senasapp.usuariospringboot.dto.UsuarioRespuesta;
import com.senasapp.usuariospringboot.model.Usuario;
import com.senasapp.usuariospringboot.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminTokenService adminTokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       AdminTokenService adminTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminTokenService = adminTokenService;
    }

    public UsuarioRespuesta registrar(RegistroRequest req) {
        String nombre = limpiar(req.nombre());
        String email = limpiar(req.email()).toLowerCase();
        String password = req.password() == null ? "" : req.password();

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nombre.length() > 50) {
            throw new IllegalArgumentException("El nombre es demasiado largo (máximo 50 caracteres)");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (password.length() > 30) {
            throw new IllegalArgumentException("La contraseña es demasiado larga (máximo 30 caracteres)");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("Ese correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol("oyente");
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public Optional<LoginRespuesta> login(LoginRequest req) {
        String email = limpiar(req.email()).toLowerCase();
        String password = req.password() == null ? "" : req.password();

        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()));

        if (encontrado.isPresent() && !encontrado.get().isActivo()) {
            throw new IllegalStateException("Tu cuenta está desactivada. Contacta al administrador.");
        }
        // Si es administrador, Spring le pide su sesión a Django para que entre directo al panel
        return encontrado.map(u -> {
            String tokenAdmin = esAdmin(u) ? adminTokenService.pedirToken(email, password) : null;
            return LoginRespuesta.de(aRespuesta(u), tokenAdmin);
        });
    }

    private boolean esAdmin(Usuario u) {
        String rol = u.getRol() == null ? "" : u.getRol().trim();
        return rol.equalsIgnoreCase("admin") || rol.equalsIgnoreCase("administrador");
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private UsuarioRespuesta aRespuesta(Usuario u) {
        return new UsuarioRespuesta(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol(), u.getFoto());
    }
}