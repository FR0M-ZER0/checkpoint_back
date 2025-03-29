package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.HorasExtras;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HorasExtrasRepository extends JpaRepository<HorasExtras, Long> {
    // Alterado para usar Long
    List<HorasExtras> findByColaboradorId(Long colaboradorId);
}