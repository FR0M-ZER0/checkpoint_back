package com.fromzero.checkpoint.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fromzero.checkpoint.entities.SolicitacaoAjustePonto;

public interface SolicitacaoAjustePontoRepository extends MongoRepository<SolicitacaoAjustePonto, String> {
    List<SolicitacaoAjustePonto> findByStatus(SolicitacaoAjustePonto.StatusMarcacao status);

    long countByStatus(SolicitacaoAjustePonto.StatusMarcacao status);

    long countByCriadoEmBetween(LocalDateTime start, LocalDateTime end);

    List<SolicitacaoAjustePonto> findTop4ByStatusOrderByCriadoEmDesc(SolicitacaoAjustePonto.StatusMarcacao status);
}
