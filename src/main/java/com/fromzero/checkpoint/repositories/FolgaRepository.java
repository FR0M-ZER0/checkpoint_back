package com.fromzero.checkpoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Folga;

public interface FolgaRepository extends JpaRepository<Folga, Long>{
    List<Folga> findByColaboradorId(Long colaboradorId);
    Long countByColaboradorId(Long colaboradorId);
}
