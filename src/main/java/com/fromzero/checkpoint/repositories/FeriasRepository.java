package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.entities.Marcacao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FeriasRepository extends JpaRepository<Ferias, Long> {
	
	List<Ferias> findByColaboradorIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(Long colaboradorId, LocalDate dataInicio, LocalDate dataFim);
	
    List<Ferias> findByColaboradorId(Long colaboradorId);
    
    // Adicione este método
    List<Ferias> findByAprovado(Boolean aprovado);
    
    // Método alternativo mais específico (opcional)
    List<Ferias> findByColaboradorIdAndAprovado(Long colaboradorId, Boolean aprovado);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Ferias f WHERE f.colaboradorId = :colaboradorId AND :data BETWEEN f.dataInicio AND f.dataFim AND f.aprovado = true")
    boolean existsByColaboradorIdAndData(@Param("colaboradorId") Long colaboradorId, @Param("data") LocalDate data);
}