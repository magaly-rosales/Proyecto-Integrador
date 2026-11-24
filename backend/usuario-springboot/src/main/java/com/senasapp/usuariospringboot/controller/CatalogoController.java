package com.senasapp.usuariospringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Catálogo "Señas disponibles": solo señas entrenadas, con sus categorías y el total por categoría.
// Todo se calcula aquí; la web solo muestra lo que recibe.
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    // Misma meta que usa el panel de administrador (senas/progreso.py)
    private static final int META_MUESTRAS = 2;
    private static final int META_MOVIMIENTOS = 2;

    private static final String SQL_SENAS = """
            SELECT s.id_sena AS id,
                   s.nombre,
                   s.descripcion,
                   s.imagen,
                   s.fecha_registro AS fecha,
                   (s.fecha_registro >= NOW() - INTERVAL '7 days') AS nueva,
                   c.id_categoria AS "idCategoria",
                   COALESCE(c.nombre, s.categoria) AS categoria,
                   c.color,
                   c.icono,
                   c.imagen AS "imagenCategoria",
                   (SELECT v.url_video FROM video_referencia v
                     WHERE v.id_sena = s.id_sena
                     ORDER BY v.fecha_subida DESC LIMIT 1) AS video
              FROM sena s
              LEFT JOIN categoria c ON c.id_categoria = s.id_categoria
             WHERE (SELECT COUNT(*) FROM muestra_sena m WHERE m.id_sena = s.id_sena) >= ?
                OR (SELECT COUNT(*) FROM muestra_secuencia q WHERE q.id_sena = s.id_sena) >= ?
             ORDER BY s.fecha_registro DESC, s.id_sena DESC
            """;

    @Autowired
    private JdbcTemplate jdbc;

    @GetMapping
    public Map<String, Object> catalogo() {
        List<Map<String, Object>> senas = jdbc.queryForList(SQL_SENAS, META_MUESTRAS, META_MOVIMIENTOS);

        // Categorías con su total de señas entrenadas (solo las que tienen al menos una)
        Map<String, Map<String, Object>> porCategoria = new LinkedHashMap<>();
        for (Map<String, Object> sena : senas) {
            String nombre = String.valueOf(sena.get("categoria"));
            Map<String, Object> cat = porCategoria.computeIfAbsent(nombre, k -> {
                Map<String, Object> nueva = new LinkedHashMap<>();
                nueva.put("id", sena.get("idCategoria"));
                nueva.put("nombre", k);
                nueva.put("color", sena.get("color"));
                nueva.put("icono", sena.get("icono"));
                nueva.put("imagen", sena.get("imagenCategoria"));
                nueva.put("total", 0);
                nueva.put("hayNuevas", false);
                return nueva;
            });
            cat.put("total", (Integer) cat.get("total") + 1);
            if (Boolean.TRUE.equals(sena.get("nueva"))) cat.put("hayNuevas", true);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("total", senas.size());
        respuesta.put("categorias", porCategoria.values());
        respuesta.put("senas", senas);
        return respuesta;
    }
}
