package com.fromzero.checkpoint.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;

@Repository
public interface SolicitacaoFolgaRepository extends JpaRepository<SolicitacaoFolga, Integer> {
    List<SolicitacaoFolga> findByColaboradorId(Long colaboradorId);

    Optional<SolicitacaoFolga> findByColaboradorIdAndSolFolData(Long colaboradorId, LocalDate solFolData);
    
}