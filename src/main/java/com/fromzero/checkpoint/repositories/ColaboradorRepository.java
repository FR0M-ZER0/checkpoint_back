package com.fromzero.checkpoint.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fromzero.checkpoint.entities.Colaborador;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    Optional<Colaborador> findByEmail(String email);
    List<Colaborador> findByNomeContainingIgnoreCase(String nome);
    List<Colaborador> findByAtivo(Boolean ativo);
    List<Colaborador> findAll(Sort sort);
    int countByAtivoIsTrueAndCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);
}