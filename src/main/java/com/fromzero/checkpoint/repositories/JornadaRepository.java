package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Jornada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface JornadaRepository extends JpaRepository<Jornada, Long> {
    List<Jornada> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Jornada> findByColaboradorId(Long colaboradorId);
}