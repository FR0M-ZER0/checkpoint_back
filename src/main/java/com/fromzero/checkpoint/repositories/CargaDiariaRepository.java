package com.fromzero.checkpoint.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fromzero.checkpoint.entities.CargaDiaria;

public interface CargaDiariaRepository extends JpaRepository<CargaDiaria, Long> {
}