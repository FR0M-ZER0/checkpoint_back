package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.models.Colaborador;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeriasService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    public Double obterSaldoFerias(Integer colaboradorId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com o ID: " + colaboradorId));
        return colaborador.getSaldoFerias();
    }

    // Adicionaremos a lógica para descontar o saldo de férias aqui posteriormente
    // public void descontarSaldoFerias(Integer colaboradorId, Double dias) { ... }
}