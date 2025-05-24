package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.entities.HorasExtras.Status;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HorasExtrasRepository extends JpaRepository<HorasExtras, Long> {
    List<HorasExtras> findByColaboradorId(Long colaboradorId);
    
    List<HorasExtras> findByColaboradorIdAndStatus(Long colaboradorId, Status status);
    
    List<HorasExtras> findByColaboradorIdAndStatusAndCriadoEmBetween(
            Long colaboradorId, 
            Status status,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    List<HorasExtras> findByStatusAndCriadoEmBetween(HorasExtras.Status status, LocalDateTime inicio, LocalDateTime fim);
}