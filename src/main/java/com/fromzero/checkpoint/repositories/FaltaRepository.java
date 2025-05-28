package com.fromzero.checkpoint.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Falta f WHERE f.colaborador.id = :colaboradorId AND DATE(f.criadoEm) = :data")
    boolean existsByColaboradorIdAndData(@Param("colaboradorId") Long colaboradorId, @Param("data") LocalDate data);

    @Query("SELECT f FROM Falta f WHERE f.criadoEm BETWEEN :inicio AND :fim "
        + "AND (:justificado IS NULL OR f.justificado = :justificado) "
        + "AND (:tipo IS NULL OR f.tipo = :tipo)")
    List<Falta> findByFilters(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("justificado") Boolean justificado,
        @Param("tipo") Falta.TipoFalta tipo
    );

    List<Falta> findByColaboradorIdAndCriadoEmBetween(Long colaboradorId, LocalDateTime dataInicio, LocalDateTime dataFim);
}
