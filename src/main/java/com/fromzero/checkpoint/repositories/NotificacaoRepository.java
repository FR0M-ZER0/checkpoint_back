package com.fromzero.checkpoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByColaboradorIdAndLidaFalse(Long colaboradorId);
}
