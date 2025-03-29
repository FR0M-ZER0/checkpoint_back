package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.HorasExtras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HorasExtrasRepository extends JpaRepository<HorasExtras, Long> {

    // Método existente (da HEAD)
    List<HorasExtras> findByColaboradorId(Long colaboradorId);

    // Novo método (da branch 80da494)
    List<HorasExtras> findByColaboradorIdAndCriadoEmAfter(Long colaboradorId, LocalDateTime data);
}