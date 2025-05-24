package com.fromzero.checkpoint.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fromzero.checkpoint.entities.Falta;

public interface FaltaRepository extends JpaRepository<Falta, Long> {
    List<Falta> findByColaboradorId(Long colaboradorId);

    @Query("SELECT f FROM Falta f WHERE f.colaborador.id = :colaboradorId AND f.id NOT IN " +
       "(SELECT s.falta.id FROM SolicitacaoAbonoFalta s)")
    List<Falta> obterFaltasSemSolicitacao(Long colaboradorId);

    Long countByColaboradorId(Long colaboradorId);

    boolean existsByColaboradorIdAndTipoAndCriadoEmBetween(
        Long colaboradorId, 
        Falta.TipoFalta tipo, 
        LocalDateTime startDateTime, 
        LocalDateTime endDateTime
    );

    int countByCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);
}
