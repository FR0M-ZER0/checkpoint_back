package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.SolicitacaoFerias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- ADICIONE ESTA LINHA

public interface SolicitacaoFeriasRepository extends JpaRepository<SolicitacaoFerias, Long> {

    List<SolicitacaoFerias> findByColaboradorId(Long colaboradorId);
}