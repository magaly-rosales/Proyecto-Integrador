package com.senasapp.usuariospringboot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_traduccion")
public class RegistroTraduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;

    @Column(name = "id_sena")
    private Integer idSena;

    @Column(name = "nombre_sena", nullable = false, length = 100)
    private String nombreSena;

    // la fecha la pone la base de datos (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    public Integer getIdRegistro() { return idRegistro; }
    public Integer getIdSena() { return idSena; }
    public void setIdSena(Integer idSena) { this.idSena = idSena; }
    public String getNombreSena() { return nombreSena; }
    public void setNombreSena(String nombreSena) { this.nombreSena = nombreSena; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}