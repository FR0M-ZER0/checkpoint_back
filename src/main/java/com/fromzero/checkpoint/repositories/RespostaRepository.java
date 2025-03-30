package com.fromzero.checkpoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Resposta;

public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    List<Resposta> findByColaboradorIdAndLidaFalse(Long colaboradorId);
}
