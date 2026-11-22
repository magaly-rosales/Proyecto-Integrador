package com.senasapp.usuariospringboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// Pide a Django (panel del administrador) la sesión del admin que acaba de iniciar sesión
@Service
public class AdminTokenService {

    private final RestClient cliente;

    public AdminTokenService(@Value("${admin.django.url}") String urlDjango) {
        HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        this.cliente = RestClient.builder()
                .baseUrl(urlDjango)
                .requestFactory(new JdkClientHttpRequestFactory(http))
                .build();
    }

    public String pedirToken(String email, String password) {
        // El cuerpo se arma con su tamaño exacto: Django (runserver) no acepta envíos "chunked"
        byte[] cuerpo = ("{\"email\":\"" + escapar(email) + "\",\"password\":\"" + escapar(password) + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        try {
            Map<String, Object> respuesta = cliente.post()
                    .uri("/api/login/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(cuerpo.length)
                    .body(cuerpo)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            Object token = respuesta == null ? null : respuesta.get("token");
            if (token == null) {
                throw new IllegalStateException("El panel de administración no entregó la sesión.");
            }
            return token.toString();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "El panel de administración rechazó el acceso (código " + e.getStatusCode().value() + ").");
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "No se pudo conectar con el panel de administración. Verifica que Django esté encendido.");
        }
    }

    // Escapa comillas, barras y caracteres de control para que el JSON sea válido
    private String escapar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}