package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Jornada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JornadaRepository extends JpaRepository<Jornada, Long> { 
}