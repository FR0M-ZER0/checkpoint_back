package com.fromzero.checkpoint.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Folga;

public interface FolgaRepository extends JpaRepository<Folga, Long>{
    List<Folga> findByColaboradorId(Long colaboradorId);
    List<Folga> findByDataBetween(LocalDate inicio, LocalDate fim);
    Long countByColaboradorId(Long colaboradorId);
    boolean existsByColaboradorIdAndData(Long colaboradorId, LocalDate data);
}
