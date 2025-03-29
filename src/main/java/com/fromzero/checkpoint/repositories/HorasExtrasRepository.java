package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.HorasExtras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HorasExtrasRepository extends JpaRepository<HorasExtras, Long> {

    // Busca todas as horas extras de um colaborador a partir de uma data específica
    List<HorasExtras> findByColaboradorIdAndCriadoEmAfter(Long colaboradorId, LocalDateTime data);

    // (opcional) busca todas as horas extras de um colaborador
    List<HorasExtras> findByColaboradorId(Long colaboradorId);

    List<HorasExtras> findByColaboradorIdAndCriadoEmBetween(Long colaboradorId, LocalDateTime inicio, LocalDateTime fim);
    
}
