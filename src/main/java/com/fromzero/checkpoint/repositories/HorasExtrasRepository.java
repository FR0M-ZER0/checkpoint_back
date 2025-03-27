package com.fromzero.checkpoint.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fromzero.checkpoint.models.HorasExtras;

@Repository
public interface HorasExtrasRepository extends JpaRepository<HorasExtras, Integer> {
    List<HorasExtras> findByColaboradorId(Integer colaboradorId);
}