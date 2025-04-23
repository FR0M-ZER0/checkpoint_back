package com.fromzero.checkpoint.services;

// ***** IMPORTS IMPORTANTES *****
import com.fromzero.checkpoint.entities.*;
// ******************************
import com.fromzero.checkpoint.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
// ChronoUnit não é mais necessário em agendarFerias (solicitação)
import java.util.List;
import java.util.Objects;


@Service
public class FeriasService {

    private static final Logger log = LoggerFactory.getLogger(FeriasService.class);

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private AbonoFeriasRepository solicitacaoAbonoFeriasRepository; // Para o método venderFerias

    @Autowired
    private FeriasRepository feriasRepository; // Pode ser útil para outras buscas no futuro

    // ***** INJETAR O NOVO REPOSITÓRIO *****
    @Autowired
    private SolicitacaoFeriasRepository solicitacaoFeriasRepository;
    // **************************************

    // Constantes (manter as de venda)
    private static final int LIMITE_DIAS_VENDA_ANO = 10;
    private static final String STATUS_VENDA_APROVADA = "APROVADO";

    public List<SolicitacaoFerias> buscarSolicitacoesPorStatus(String status) {
        log.info("Buscando solicitações de férias com status: {}", status);
        // ***** CHAMA O MÉTODO COM JOIN FETCH *****
        List<SolicitacaoFerias> solicitacoes = solicitacaoFeriasRepository.findByStatusIgnoreCaseWithColaborador(status);
        // ****************************************
        log.info("Encontradas {} solicitações com colaborador.", solicitacoes.size());
        return solicitacoes;
    }
    // --- Método obterSaldoFerias (permanece igual) ---
    public Double obterSaldoFerias(Long colaboradorId) {
        log.info("Buscando saldo de férias para colaborador ID: {}", colaboradorId);
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> {
                    log.error("Colaborador com ID {} não encontrado ao buscar saldo.", colaboradorId);
                    return new EntityNotFoundException("Colaborador com ID " + colaboradorId + " não encontrado");
                });

        if (colaborador.getSaldoFerias() == null) {
            log.warn("Saldo de férias NULO para o colaborador ID: {}. Retornando 0.0", colaboradorId);
             return 0.0;
        }
        log.info("Saldo encontrado para colaborador ID {}: {}", colaboradorId, colaborador.getSaldoFerias());
        return colaborador.getSaldoFerias();
    }

    // --- Método venderFerias (permanece igual, com validação de limite) ---
    @Transactional
    public SolicitacaoAbonoFerias venderFerias(SolicitacaoAbonoFerias solicitacao) {
        log.info("Iniciando processo de venda de férias: {}", solicitacao);

        Objects.requireNonNull(solicitacao.getColaboradorId(), "ID do Colaborador não pode ser nulo na solicitação de venda.");
        Objects.requireNonNull(solicitacao.getDiasVendidos(), "Quantidade de dias vendidos não pode ser nula.");
        if (solicitacao.getDiasVendidos() <= 0) {
            throw new IllegalArgumentException("A quantidade de dias a vender deve ser positiva.");
        }
        if (solicitacao.getDataSolicitacao() == null) solicitacao.setDataSolicitacao(LocalDate.now());
        if (solicitacao.getStatus() == null) solicitacao.setStatus("PENDENTE");

        Colaborador colaborador = colaboradorRepository.findById(solicitacao.getColaboradorId())
                .orElseThrow(() -> {
                     log.error("Colaborador com ID {} não encontrado para venda de férias.", solicitacao.getColaboradorId());
                     return new EntityNotFoundException("Colaborador com ID " + solicitacao.getColaboradorId() + " não encontrado");
                 });
        log.info("Colaborador encontrado: ID {}, Saldo atual: {}", colaborador.getId(), colaborador.getSaldoFerias());

        LocalDate dataInicioPeriodo = solicitacao.getDataSolicitacao().minusYears(1);
        List<SolicitacaoAbonoFerias> vendasAnteriores = solicitacaoAbonoFeriasRepository
                .findByColaboradorIdAndDataSolicitacaoAfter(colaborador.getId(), dataInicioPeriodo);

        int diasJaVendidosNoPeriodo = vendasAnteriores.stream()
                 .filter(venda -> STATUS_VENDA_APROVADA.equalsIgnoreCase(venda.getStatus())) // CONFIRME SE ESSE É O STATUS CORRETO PARA VENDA APROVADA
                .mapToInt(SolicitacaoAbonoFerias::getDiasVendidos)
                .sum();
        log.info("Dias já vendidos pelo colaborador ID {} no último ano (status {}): {}", colaborador.getId(), STATUS_VENDA_APROVADA, diasJaVendidosNoPeriodo);

        if (diasJaVendidosNoPeriodo + solicitacao.getDiasVendidos() > LIMITE_DIAS_VENDA_ANO) {
            log.warn("Tentativa de venda excedeu o limite de {} dias. Já vendidos: {}, Tentando vender: {}",
                    LIMITE_DIAS_VENDA_ANO, diasJaVendidosNoPeriodo, solicitacao.getDiasVendidos());
            throw new IllegalArgumentException(String.format(
                    "Limite de %d dias para venda de férias no período excedido. Você já vendeu %d dias.",
                    LIMITE_DIAS_VENDA_ANO, diasJaVendidosNoPeriodo
            ));
        }

        if (colaborador.getSaldoFerias() == null || colaborador.getSaldoFerias() < solicitacao.getDiasVendidos()) {
            log.warn("Saldo insuficiente para venda. Saldo: {}, Dias solicitados: {}", colaborador.getSaldoFerias(), solicitacao.getDiasVendidos());
            throw new IllegalStateException("Saldo de férias insuficiente para vender " + solicitacao.getDiasVendidos() + " dias.");
        }

        Double novoSaldo = colaborador.getSaldoFerias() - solicitacao.getDiasVendidos();
        colaborador.setSaldoFerias(novoSaldo);
        colaboradorRepository.save(colaborador);
        log.info("Saldo do colaborador ID {} atualizado para: {}", colaborador.getId(), novoSaldo);

        SolicitacaoAbonoFerias abonoSalvo = solicitacaoAbonoFeriasRepository.save(solicitacao);
        log.info("Solicitação de venda de férias salva com sucesso: {}", abonoSalvo);
        return abonoSalvo;
    }


    // --- Agendar Férias (VERSÃO CORRIGIDA PARA CRIAR SOLICITAÇÃO) ---
    @Transactional
    // ***** ASSINATURA CORRIGIDA *****
    public SolicitacaoFerias agendarFerias(SolicitacaoFerias solicitacao) { // RECEBE E RETORNA SolicitacaoFerias
        log.info("Iniciando processo de SOLICITAÇÃO de férias: {}", solicitacao);

        // Validações básicas da SOLICITAÇÃO
        Objects.requireNonNull(solicitacao.getColaboradorId(), "ID do Colaborador não pode ser nulo na solicitação.");
        Objects.requireNonNull(solicitacao.getDataInicio(), "Data de início não pode ser nula.");
        Objects.requireNonNull(solicitacao.getDataFim(), "Data fim não pode ser nula.");
        // Define status padrão se não vier no payload
        if (solicitacao.getStatus() == null || solicitacao.getStatus().trim().isEmpty()) {
            solicitacao.setStatus("PENDENTE");
            log.info("Status da solicitação definido como PENDENTE por padrão.");
        }

        if (solicitacao.getDataFim().isBefore(solicitacao.getDataInicio())) {
            throw new IllegalArgumentException("Data fim não pode ser anterior à data início.");
        }

        // Verifica se o colaborador existe
        if (!colaboradorRepository.existsById(solicitacao.getColaboradorId())) {
             log.error("Colaborador com ID {} não encontrado ao tentar criar solicitação de férias.", solicitacao.getColaboradorId());
             throw new EntityNotFoundException("Colaborador com ID " + solicitacao.getColaboradorId() + " não encontrado");
        }
        log.info("Colaborador ID {} encontrado. Criando solicitação...", solicitacao.getColaboradorId());

        // NÃO HÁ LÓGICA DE SALDO AQUI (isso é para a aprovação)

        // Salva a SOLICITAÇÃO usando o repositório correto
        SolicitacaoFerias solicitacaoSalva = solicitacaoFeriasRepository.save(solicitacao); // <-- USA O REPOSITÓRIO CORRETO
        log.info("Solicitação de férias salva com sucesso: {}", solicitacaoSalva);
        return solicitacaoSalva; // <-- RETORNA O OBJETO CORRETO
    }
}