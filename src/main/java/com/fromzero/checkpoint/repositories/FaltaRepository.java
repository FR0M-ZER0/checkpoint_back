package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Jornada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FaltaRepository extends JpaRepository<Falta, Long> {
    List<Falta> findByColaboradorId(Long colaboradorId);
    
    Optional<Falta> findByJornadaAndTipo(Jornada jornada, Falta.TipoFalta tipo);

    @Query("SELECT f FROM Falta f WHERE f.colaborador.id = :colaboradorId AND f.id NOT IN " +
       "(SELECT s.falta.id FROM SolicitacaoAbonoFalta s)")
    List<Falta> obterFaltasSemSolicitacao(Long colaboradorId);
}