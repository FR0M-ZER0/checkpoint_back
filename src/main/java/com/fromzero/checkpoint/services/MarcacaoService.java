package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.MarcacaoLog;
import com.fromzero.checkpoint.repositories.MarcacaoLogRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarcacaoService {

    private static final Logger logger = LoggerFactory.getLogger(MarcacaoService.class);

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Autowired
    private MarcacaoLogRepository marcacaoLogRepository;

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

        // Criar log
        MarcacaoLog log = new MarcacaoLog(marcacao.getColaboradorId(), "CRIACAO", marcacao.getTipo());
        marcacaoLogRepository.save(log);

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

        // Criar log de exclusão
        MarcacaoLog log = new MarcacaoLog(marcacao.getColaboradorId(), "DELECAO", marcacao.getTipo());
        marcacaoLogRepository.save(log);
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

    // Calcular total de horas trabalhadas por mês
    public Map<String, String> calcularHorasTrabalhadasPorMes(Long colaboradorId) {
        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                colaboradorId,
                LocalDate.of(2000, 1, 1).atStartOfDay(),
                LocalDateTime.now()
        );

        Map<String, Duration> totalPorMes = new HashMap<>();

        marcacoes.stream()
                .collect(Collectors.groupingBy(m -> m.getDataHora().toLocalDate()))
                .forEach((data, listaDoDia) -> {
                    listaDoDia.sort(Comparator.comparing(Marcacao::getDataHora));

                    Duration totalDia = Duration.ZERO;
                    LocalDateTime entrada = null;
                    LocalDateTime pausa = null;

                    for (Marcacao m : listaDoDia) {
                        switch (m.getTipo()) {
                            case ENTRADA:
                                entrada = m.getDataHora();
                                break;
                            case PAUSA:
                                if (entrada != null) {
                                    totalDia = totalDia.plus(Duration.between(entrada, m.getDataHora()));
                                    entrada = null;
                                    pausa = m.getDataHora();
                                }
                                break;
                            case RETOMADA:
                                pausa = m.getDataHora();
                                break;
                            case SAIDA:
                                if (pausa != null) {
                                    totalDia = totalDia.plus(Duration.between(pausa, m.getDataHora()));
                                    pausa = null;
                                }
                                break;
                        }
                    }

                    String mesAno = data.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + "/" + data.getYear();
                    totalPorMes.put(mesAno, totalPorMes.getOrDefault(mesAno, Duration.ZERO).plus(totalDia));
                });

        // Converter Duration para string legível
        Map<String, String> resultado = new LinkedHashMap<>();
        totalPorMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    long horas = entry.getValue().toHours();
                    long minutos = entry.getValue().toMinutes() % 60;
                    resultado.put(entry.getKey(), String.format("%dh%02dm", horas, minutos));
                });

        return resultado;
    }

    // Calcular total de horas trabalhadas no dia atual
    public Duration calcularHorasTrabalhadasHoje(Long colaboradorId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);

        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicio, fim);

        marcacoes = marcacoes.stream()
                .sorted(Comparator.comparing(Marcacao::getDataHora))
                .collect(Collectors.toList());

        Duration total = Duration.ZERO;
        LocalDateTime entrada = null;
        LocalDateTime pausa = null;

        for (Marcacao m : marcacoes) {
            switch (m.getTipo()) {
                case ENTRADA:
                    entrada = m.getDataHora();
                    break;
                case PAUSA:
                    if (entrada != null) {
                        total = total.plus(Duration.between(entrada, m.getDataHora()));
                        entrada = null;
                        pausa = m.getDataHora();
                    }
                    break;
                case RETOMADA:
                    pausa = m.getDataHora();
                    break;
                case SAIDA:
                    if (pausa != null) {
                        total = total.plus(Duration.between(pausa, m.getDataHora()));
                        pausa = null;
                    }
                    break;
            }
        }

        return total;
    }
}
