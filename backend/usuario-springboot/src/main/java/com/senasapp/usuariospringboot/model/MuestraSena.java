package com.senasapp.usuariospringboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "muestra_sena")
public class MuestraSena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_muestra")
    private Integer idMuestra;

    @ManyToOne
    @JoinColumn(name = "id_sena", nullable = false)
    private Sena sena;

    @Column(name = "puntos", nullable = false, columnDefinition = "TEXT")
    private String puntos;

    public Integer getIdMuestra() { return idMuestra; }
    public Sena getSena() { return sena; }
    public void setSena(Sena sena) { this.sena = sena; }
    public String getPuntos() { return puntos; }
    public void setPuntos(String puntos) { this.puntos = puntos; }
}