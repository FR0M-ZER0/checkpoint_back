package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Ferias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeriasRepository extends JpaRepository<Ferias, Long> {
    
    List<Ferias> findByColaboradorId(Long colaboradorId);
    
    // Adicione este método
    List<Ferias> findByAprovado(Boolean aprovado);
    
    // Método alternativo mais específico (opcional)
    List<Ferias> findByColaboradorIdAndAprovado(Long colaboradorId, Boolean aprovado);
}