package com.senasapp.usuariospringboot.repository;

import com.senasapp.usuariospringboot.model.RegistroTraduccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroTraduccionRepository extends JpaRepository<RegistroTraduccion, Integer> {
}