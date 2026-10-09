package com.senasapp.usuariospringboot.repository;

import com.senasapp.usuariospringboot.model.Sena;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SenaRepository extends JpaRepository<Sena, Integer> {
}