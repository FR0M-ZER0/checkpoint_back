package com.fromzero.checkpoint.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fromzero.checkpoint.entities.SolicitacaoAjustePonto;

public interface SolicitacaoAjustePontoRepository extends MongoRepository<SolicitacaoAjustePonto, String> {
    List<SolicitacaoAjustePonto> findByStatus(SolicitacaoAjustePonto.StatusMarcacao status);
}
