package com.senasapp.usuariospringboot.repository;

import com.senasapp.usuariospringboot.model.Sena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SenaRepository extends JpaRepository<Sena, Integer> {
    Optional<Sena> findFirstByNombreIgnoreCase(String nombre);
}