package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Ferias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeriasRepository extends JpaRepository<Ferias, Long> {
    
    // Busca férias por colaborador
    List<Ferias> findByColaboradorId(Long colaboradorId);
}