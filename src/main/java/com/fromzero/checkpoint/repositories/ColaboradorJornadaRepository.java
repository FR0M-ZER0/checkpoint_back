package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.ColaboradorJornada; // <<< IMPORTA A ENTIDADE
import org.springframework.data.jpa.repository.JpaRepository; // <<< IMPORTA JPA REPOSITORY
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

public interface ColaboradorJornadaRepository extends JpaRepository<ColaboradorJornada, Long> {

    // Seu método existente para buscar a jornada ativa em UMA data específica
    @Query("SELECT cj FROM ColaboradorJornada cj " +
           "JOIN FETCH cj.jornada j " +
           "WHERE cj.colaboradorId = :colaboradorId " +
           "AND cj.dataInicio <= :date " +
           "AND (cj.dataFim IS NULL OR cj.dataFim >= :date) " +
           "ORDER BY cj.dataInicio DESC")
    List<ColaboradorJornada> findActiveJornadaForColaboradorAtDate(
        @Param("colaboradorId") Long colaboradorId,
        @Param("date") LocalDate date
    );

    // ***** NOVO MÉTODO OTIMIZADO *****
    @Query("SELECT cj FROM ColaboradorJornada cj " +
           "JOIN FETCH cj.jornada j " + 
           "WHERE cj.colaboradorId = :colaboradorId " +
           "AND cj.dataInicio <= :endDateOfYear " +
           "AND (cj.dataFim IS NULL OR cj.dataFim >= :startDateOfYear)")
    List<ColaboradorJornada> findAllRelevantForColaboradorInYear( // Nome um pouco mais genérico
        @Param("colaboradorId") Long colaboradorId,
        @Param("startDateOfYear") LocalDate startDateOfYear,
        @Param("endDateOfYear") LocalDate endDateOfYear
    );
}