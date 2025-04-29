package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.SolicitacaoFerias;
import org.springframework.data.domain.Page; // Import se usar paginação em outros métodos
import org.springframework.data.domain.Pageable; // Import se usar paginação
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoFeriasRepository extends JpaRepository<SolicitacaoFerias, Long> {

    List<SolicitacaoFerias> findByColaboradorId(Long colaboradorId);

    // Método para buscar por status com JOIN FETCH (para a lista do admin)
    @Query(value = "SELECT sf FROM SolicitacaoFerias sf JOIN FETCH sf.colaborador WHERE lower(sf.status) = lower(:status)",
           countQuery = "SELECT count(sf) FROM SolicitacaoFerias sf WHERE lower(sf.status) = lower(:status)")
    Page<SolicitacaoFerias> findByStatusIgnoreCaseWithColaborador(@Param("status") String status, Pageable pageable);

    // ***** ADICIONE ESTE MÉTODO *****
    // Método para buscar por ID com JOIN FETCH (para aprovar/rejeitar)
    @Query("SELECT sf FROM SolicitacaoFerias sf JOIN FETCH sf.colaborador WHERE sf.id = :id")
    Optional<SolicitacaoFerias> findByIdWithColaborador(@Param("id") Long id);
    // ******************************
}