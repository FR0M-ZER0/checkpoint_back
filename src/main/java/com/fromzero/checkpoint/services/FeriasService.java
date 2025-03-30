package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.*;
import com.fromzero.checkpoint.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FeriasService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;
    
    @Autowired
    private AbonoFeriasRepository solicitacaoAbonoFeriasRepository;
    
    @Autowired
    private FeriasRepository feriasRepository;

    public Double obterSaldoFerias(Long colaboradorId) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
            .orElseThrow(() -> new RuntimeException("Colaborador com ID " + colaboradorId + " não encontrado"));
        
        if (colaborador.getSaldoFerias() == null) {
            throw new RuntimeException("Saldo de férias não disponível para o colaborador");
        }
        
        return colaborador.getSaldoFerias();
    }

    @Transactional
    public SolicitacaoAbonoFerias venderFerias(SolicitacaoAbonoFerias solicitacao) {
        System.out.println("venderFerias iniciado: " + solicitacao);
        System.out.println("Colaborador ID: " + solicitacao.getColaboradorId());
        System.out.println("Dias Vendidos: " + solicitacao.getDiasVendidos());
        System.out.println("Data Solicitação: " + solicitacao.getDataSolicitacao());
        System.out.println("Status Solicitação: " + solicitacao.getStatus());
        try {
            // Busca o colaborador
            Colaborador colaborador = colaboradorRepository.findById(solicitacao.getColaboradorId())
                    .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
    
            System.out.println("Colaborador Encontrado: " + colaborador);
            System.out.println("Saldo Férias Colaborador: " + colaborador.getSaldoFerias());
    
            // Validar saldo
            if (colaborador.getSaldoFerias() < solicitacao.getDiasVendidos()) {
                System.out.println("Saldo Insuficiente");
                throw new RuntimeException("Saldo de férias insuficiente.");
            }
    
            // Atualizar saldo do colaborador
            Double novoSaldo = colaborador.getSaldoFerias() - solicitacao.getDiasVendidos();
            colaborador.setSaldoFerias(novoSaldo);
            System.out.println("Novo Saldo: " + novoSaldo);
            colaboradorRepository.save(colaborador);
    
            // Salva a solicitação
            SolicitacaoAbonoFerias abonoSalvo = solicitacaoAbonoFeriasRepository.save(solicitacao);
            System.out.println("Solicitacao salva: " + abonoSalvo);
            return abonoSalvo;
        } catch (Exception e) {
            System.err.println("Erro em venderFerias: " + e.getMessage());
            e.printStackTrace(); // Imprime a pilha de chamadas
            throw e; // Propaga a exceção para o Controller
        }
    }

    @Transactional
    public Ferias agendarFerias(Ferias ferias) {
        // Validações básicas
        if (ferias.getDataInicio() == null || ferias.getDataFim() == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (ferias.getDataFim().isBefore(ferias.getDataInicio())) {
            throw new IllegalArgumentException("Data fim não pode ser anterior à data início");
        }

        // Busca o colaborador
        Colaborador colaborador = colaboradorRepository.findById(ferias.getColaboradorId())
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

        // Calcula dias solicitados
        long diasSolicitados = ChronoUnit.DAYS.between(ferias.getDataInicio(), ferias.getDataFim()) + 1;
        
        // Valida saldo
        if (colaborador.getSaldoFerias() < diasSolicitados) {
            throw new RuntimeException("Saldo de férias insuficiente");
        }

        // Salva a solicitação
        return feriasRepository.save(ferias);
    }
}