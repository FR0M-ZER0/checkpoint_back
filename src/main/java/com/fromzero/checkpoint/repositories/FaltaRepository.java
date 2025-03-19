package com.fromzero.checkpoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Falta;

public interface FaltaRepository extends JpaRepository<Falta, Long> {
    List<Falta> findByColaboradorId(Long colaboradorId);
}
