package com.fromzero.checkpoint.repositories;

import com.fromzero.checkpoint.models.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Integer> {
    // Você pode adicionar métodos de consulta personalizados aqui, se necessário
    // Por exemplo, para buscar um colaborador pelo email:
    // Optional<Colaborador> findByEmail(String email);
}

