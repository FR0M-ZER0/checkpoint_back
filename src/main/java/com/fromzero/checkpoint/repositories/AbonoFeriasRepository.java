package com.fromzero.checkpoint.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFerias;

@Repository
public interface AbonoFeriasRepository extends JpaRepository<SolicitacaoAbonoFerias, Long> {

    // Agora o compilador saberá o que é List aqui também
    List<SolicitacaoAbonoFerias> findByColaboradorIdAndDataSolicitacaoAfter(Long colaboradorId, LocalDate dataInicioPeriodo);
}