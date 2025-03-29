package com.fromzero.checkpoint.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fromzero.checkpoint.entities.Gestor;

public interface GestorRepository extends JpaRepository<Gestor, Long> {
    Optional<Gestor> findByEmail(String email);
}
