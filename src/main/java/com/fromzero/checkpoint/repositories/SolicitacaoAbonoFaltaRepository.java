package com.fromzero.checkpoint.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.SolicitacaoAbonoFalta;

public interface SolicitacaoAbonoFaltaRepository extends JpaRepository<SolicitacaoAbonoFalta, Long> {
    Optional<SolicitacaoAbonoFalta> findByFaltaId(Long faltaId);
}
