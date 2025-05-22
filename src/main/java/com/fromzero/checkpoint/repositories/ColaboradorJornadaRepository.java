package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.ColaboradorJornada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List; // Importar List
// import java.util.Optional; // Removido Optional pois a query retorna List

@Repository
public interface ColaboradorJornadaRepository extends JpaRepository<ColaboradorJornada, Long> { // Ajuste Long se ID for Integer

    @Query("SELECT cj FROM ColaboradorJornada cj " +
           "JOIN FETCH cj.jornada " + // Importante para carregar os detalhes da Jornada (escala)
           "WHERE cj.colaboradorId = :colaboradorId " +
           "AND cj.dataInicio <= :date " +
           "AND (cj.dataFim IS NULL OR cj.dataFim >= :date) " +
           "ORDER BY cj.dataInicio DESC") // Pega a mais recente em caso de sobreposição
    List<ColaboradorJornada> findActiveJornadaForColaboradorAtDate(
        @Param("colaboradorId") Long colaboradorId, // Ajuste Long se necessário
        @Param("date") LocalDate date
    );
}