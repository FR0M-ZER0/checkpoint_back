package com.fromzero.checkpoint.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;

@Repository
public interface SolicitacaoFolgaRepository extends JpaRepository<SolicitacaoFolga, Integer> {
    // Métodos padrão do JpaRepository estarão disponíveis aqui
}