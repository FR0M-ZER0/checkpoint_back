package com.fromzero.checkpoint.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Colaborador;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    Optional<Colaborador> findByEmail(String email);
}
