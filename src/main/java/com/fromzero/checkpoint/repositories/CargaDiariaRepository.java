package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.CargaDiaria;
import com.fromzero.checkpoint.entities.Jornada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CargaDiariaRepository extends JpaRepository<CargaDiaria, Long> {
    List<CargaDiaria> findByJornada(Jornada jornada);
}