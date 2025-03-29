package com.fromzero.checkpoint.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFerias;

@Repository
public interface AbonoFeriasRepository extends JpaRepository<SolicitacaoAbonoFerias, Long> {
    // ... métodos para acessar dados de solicitacao_abono_ferias ...
}