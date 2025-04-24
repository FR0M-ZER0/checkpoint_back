package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.SolicitacaoFerias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
// import java.time.LocalDate; // Não precisa se não usar data aqui

public interface SolicitacaoFeriasRepository extends JpaRepository<SolicitacaoFerias, Long> {

    List<SolicitacaoFerias> findByColaboradorId(Long colaboradorId);

    // ***** ADICIONE ESTE MÉTODO (escolha UMA das opções abaixo) *****

    // Opção A: Simples (pode dar erro de Lazy Loading depois, como vimos)
    // List<SolicitacaoFerias> findByStatusIgnoreCase(String status);

    // Opção B: Recomendada (com JOIN FETCH para trazer o Colaborador junto)
    @Query("SELECT sf FROM SolicitacaoFerias sf JOIN FETCH sf.colaborador WHERE lower(sf.status) = lower(:status)")
    List<SolicitacaoFerias> findByStatusIgnoreCaseWithColaborador(@Param("status") String status);

    // *******************************************************************

}