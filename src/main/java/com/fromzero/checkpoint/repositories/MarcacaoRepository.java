package com.fromzero.checkpoint.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fromzero.checkpoint.entities.Marcacao;

import java.time.LocalDateTime;
import java.util.List;

public interface MarcacaoRepository extends MongoRepository<Marcacao, String> {
    List<Marcacao> findByColaboradorIdAndDataHoraBetween(Long colaboradorId, LocalDateTime inicio, LocalDateTime fim);

    List<Marcacao> findByColaboradorId(Long colaboradorId);
}