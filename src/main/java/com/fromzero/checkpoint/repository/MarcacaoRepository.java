package com.fromzero.checkpoint.repository;

import com.fromzero.checkpoint.model.Marcacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MarcacaoRepository extends MongoRepository<Marcacao, String> {

    List<Marcacao> findByColaboradorIdAndDataHoraBetween(Long colaboradorId, LocalDateTime inicio, LocalDateTime fim);

}