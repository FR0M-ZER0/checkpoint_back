package com.fromzero.checkpoint.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fromzero.checkpoint.model.Marcacao;
import com.fromzero.checkpoint.repository.MarcacaoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class MarcacaoService {

    private static final Logger logger = LoggerFactory.getLogger(MarcacaoService.class);

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    // Listar todas as marcações
    public List<Marcacao> listarMarcacoes() {
        return marcacaoRepository.findAll();
    }

    // Buscar marcação por id
    public Optional<Marcacao> buscarMarcacaoPorId(String id) {
        return marcacaoRepository.findById(id);
    }

    // Criar nova marcação evitando duplicações
    public Marcacao criarMarcacao(Marcacao marcacao) {
        validarMarcacaoDuplicada(marcacao);

        marcacao.setDataHora(LocalDateTime.now()); // Definir o horário de registro
        marcacao.setProcessada(false);

        Marcacao novaMarcacao = marcacaoRepository.save(marcacao);
        logger.info("Marcação registrada com sucesso: {}", novaMarcacao);
        return novaMarcacao;
    }

    // Atualizar marcação
    public Marcacao atualizarMarcacao(String id, Marcacao marcacaoAtualizada) {
        return marcacaoRepository.findById(id)
                .map(marcacao -> {
                    marcacao.setTipo(marcacaoAtualizada.getTipo());
                    marcacao.setProcessada(marcacaoAtualizada.isProcessada());
                    return marcacaoRepository.save(marcacao);
                })
                .orElseThrow(() -> {
                    logger.warn("Tentativa de atualização falhou, marcação não encontrada: {}", id);
                    return new RuntimeException("Marcação não encontrada");
                });
    }

    // Deletar marcação
    public void deletarMarcacao(String id) {
        Marcacao marcacao = marcacaoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Tentativa de exclusão falhou, marcação não encontrada: {}", id);
                    return new RuntimeException("Marcação não encontrada");
                });

        if (marcacao.isProcessada()) {
            throw new RuntimeException("Não é possível deletar uma marcação processada");
        }

        marcacaoRepository.deleteById(id);
        logger.info("Marcação deletada com sucesso: {}", id);
    }

    // Método para validar marcações duplicadas no mesmo dia
    private void validarMarcacaoDuplicada(Marcacao novaMarcacao) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.atTime(LocalTime.MAX);

        List<Marcacao> marcacoesExistentes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                novaMarcacao.getColaboradorId(), inicioDoDia, fimDoDia);

        boolean existeDuplicata = marcacoesExistentes.stream()
                .anyMatch(m -> m.getTipo().equals(novaMarcacao.getTipo()));

        if (existeDuplicata) {
            logger.warn("Tentativa de marcação duplicada para colaborador {} no mesmo dia.", novaMarcacao.getColaboradorId());
            throw new RuntimeException("Já existe uma marcação do mesmo tipo para este colaborador hoje.");
        }
    }
}