package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Falta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT f FROM Falta f WHERE f.colaborador.id = :colaboradorId " +
       "AND f.criadoEm >= :startOfYear AND f.criadoEm < :startOfNextYear")
    List<Falta> findByColaboradorIdAndCriadoEmBetweenYearRange(
    @Param("colaboradorId") Long colaboradorId,
    @Param("startOfYear") LocalDateTime startOfYear,
    @Param("startOfNextYear") LocalDateTime startOfNextYear
);
    @Query("SELECT f FROM Falta f WHERE f.colaborador.id = :colaboradorId AND DATE(f.criadoEm) = :date")
    Optional<Falta> findByColaboradorIdAndCriadoEmOnDate(
        @Param("colaboradorId") Long colaboradorId, 
        @Param("date") LocalDate date
    );
}
