package com.fromzero.checkpoint;

import com.fromzero.checkpoint.models.Ferias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeriasRepository extends JpaRepository<Ferias, Long> {
    List<Ferias> findByColaboradorId(Long colaboradorId);
}