package com.senasapp.usuariospringboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "muestra_secuencia")
public class MuestraSecuencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_secuencia")
    private Integer idSecuencia;

    @ManyToOne
    @JoinColumn(name = "id_sena", nullable = false)
    private Sena sena;

    @Column(name = "cuadros", nullable = false, columnDefinition = "TEXT")
    private String cuadros;

    @Column(name = "num_cuadros", nullable = false)
    private Integer numCuadros;

    public Integer getIdSecuencia() { return idSecuencia; }
    public Sena getSena() { return sena; }
    public void setSena(Sena sena) { this.sena = sena; }
    public String getCuadros() { return cuadros; }
    public void setCuadros(String cuadros) { this.cuadros = cuadros; }
    public Integer getNumCuadros() { return numCuadros; }
    public void setNumCuadros(Integer numCuadros) { this.numCuadros = numCuadros; }
}