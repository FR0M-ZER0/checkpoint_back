package com.fromzero.checkpoint.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.SolicitacaoAbonoFalta;

public interface SolicitacaoAbonoFaltaRepository extends JpaRepository<SolicitacaoAbonoFalta, Long> {
    Optional<SolicitacaoAbonoFalta> findByFaltaId(Long faltaId);

    long countByStatus(SolicitacaoAbonoFalta.SolicitacaoStatus status);

    long countByCriadoEmBetween(LocalDateTime start, LocalDateTime end);

    List<SolicitacaoAbonoFalta> findTop4ByStatusOrderByCriadoEmDesc(SolicitacaoAbonoFalta.SolicitacaoStatus status);
}
