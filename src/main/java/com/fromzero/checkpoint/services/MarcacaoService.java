package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.MarcacaoLog;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.entities.Resposta.TipoResposta;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.MarcacaoLogRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;

import java.time.Duration;
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

    @Autowired
    private MarcacaoLogRepository marcacaoLogRepository;

    @Autowired 
    private FaltaRepository faltaRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    // Listar todas as marcações
    public List<Marcacao> listarMarcacoes() {
        return marcacaoRepository.findAll();
    }

    public Optional<Marcacao> buscarMarcacaoPorId(String id) {
        return marcacaoRepository.findById(id);
    }

    // Buscar marcação por id
    public Marcacao criarMarcacao(Marcacao marcacao) {
        validarMarcacaoDuplicada(marcacao);
    
        marcacao.setDataHora(LocalDateTime.now()); // Definir o horário de registro
        marcacao.setProcessada(false);
    
        Marcacao novaMarcacao = marcacaoRepository.save(marcacao);
        logger.info("Marcação registrada com sucesso: {}", novaMarcacao);
    
        // Criar log
        MarcacaoLog log = new MarcacaoLog(marcacao.getColaboradorId(), "CRIACAO", marcacao.getTipo());
        marcacaoLogRepository.save(log);
    
        // Se a marcação for do tipo SAIDA, calcular total trabalhado e verificar necessidade de falta
        if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA) {
            calcularTotalTrabalhadoDia(marcacao.getColaboradorId(), LocalDate.now());
        }
    
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

    // Obter marcações do dia atual de um colaborador específico
    public List<Marcacao> obterMarcacoesDoDia(Long colaboradorId, LocalDate data) {
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        return marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDoDia, fimDoDia);
    }

    // Obter todas as marcações de um colaborador específico
    public List<Marcacao> obterTodasMarcacoesPorColaborador(Long colaboradorId) {
        return marcacaoRepository.findByColaboradorId(colaboradorId);
    }

    // Obter marcações de um dia específico de um colaborador
    public List<Marcacao> obterMarcacoesPorData(Long colaboradorId, LocalDate data) {
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        return marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDoDia, fimDoDia);
    }

    // Atualizar somente o horário de uma marcação
    public Marcacao atualizarHorarioMarcacao(String id, LocalTime novoHorario) {
        Marcacao marcacao = marcacaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
    
        if (marcacao.isProcessada()) {
            throw new RuntimeException("Não é possível alterar uma marcação processada.");
        }
    
        LocalDateTime novaDataHora = marcacao.getDataHora()
                .withHour(novoHorario.getHour())
                .withMinute(novoHorario.getMinute())
                .withSecond(0);
    
        marcacao.setDataHora(novaDataHora);
        return marcacaoRepository.save(marcacao);
    }

    public String calcularTotalTrabalhadoDia(Long colaboradorId, LocalDate dia) {
        // Obter todas as marcações de um colaborador para o dia específico
        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                colaboradorId,
                dia.atStartOfDay(),
                dia.plusDays(1).atStartOfDay()
        );

        LocalDateTime entrada = null;
        LocalDateTime ultimaSaida = null;
        Duration totalTrabalhado = Duration.ZERO;

        for (Marcacao marcacao : marcacoes) {
            if (marcacao.getTipo() == Marcacao.TipoMarcacao.ENTRADA) {
                entrada = marcacao.getDataHora();
            } else if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA && entrada != null) {
                ultimaSaida = marcacao.getDataHora();
                totalTrabalhado = totalTrabalhado.plus(Duration.between(entrada, ultimaSaida));
                entrada = null;
            }
        }

        long horas = totalTrabalhado.toHours();
        long minutos = totalTrabalhado.toMinutesPart();

        if (horas < 8) {
            boolean faltaExistente = faltaRepository.existsByColaboradorIdAndTipoAndCriadoEmBetween(
                colaboradorId, 
                Falta.TipoFalta.Atraso, 
                dia.atStartOfDay(), 
                dia.plusDays(1).atStartOfDay()
            );

            if (!faltaExistente) {
                Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                        .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

                Falta falta = new Falta();
                falta.setColaborador(colaborador);
                falta.setTipo(Falta.TipoFalta.Atraso);
                falta.setJustificado(false);
                faltaRepository.save(falta);
                notificacaoService.criaNotificacao("Você recebeu uma falta: Você não cumpriu sua carga diária", NotificacaoTipo.falta, falta.getColaborador());
            }
        }  

        return String.format("%02dh:%02dmin", horas, minutos);
    }
    public String calcularTotalTrabalhadoDiaSemFalta(Long colaboradorId, LocalDate dia) {
        // Obter todas as marcações de um colaborador para o dia específico
        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                colaboradorId,
                dia.atStartOfDay(),
                dia.plusDays(1).atStartOfDay()
        );

        LocalDateTime entrada = null;
        LocalDateTime ultimaSaida = null;
        Duration totalTrabalhado = Duration.ZERO;

        for (Marcacao marcacao : marcacoes) {
            if (marcacao.getTipo() == Marcacao.TipoMarcacao.ENTRADA) {
                entrada = marcacao.getDataHora();
            } else if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA && entrada != null) {
                ultimaSaida = marcacao.getDataHora();
                totalTrabalhado = totalTrabalhado.plus(Duration.between(entrada, ultimaSaida));
                entrada = null;
            }
        }

        long horas = totalTrabalhado.toHours();
        long minutos = totalTrabalhado.toMinutesPart();  

        return String.format("%02dh:%02dmin", horas, minutos);
    }
 
}
