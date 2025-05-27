package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fromzero.checkpoint.controllers.FeriasController;
import com.fromzero.checkpoint.dto.MarcacaoResponseDTO;
import com.fromzero.checkpoint.dto.MarcacaoResumoDTO;
import com.fromzero.checkpoint.dto.MarcacoesPorDiaDTO;
import com.fromzero.checkpoint.dto.UltimaMarcacaoResumoDTO;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.MarcacaoLog;
import com.fromzero.checkpoint.entities.Marcacao.TipoMarcacao;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.entities.Resposta.TipoResposta;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.MarcacaoLogRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private FolgaRepository folgaRepository;

    @Autowired
    private FeriasRepository feriasRepository;

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
 
    private MarcacaoResponseDTO toMarcacaoResponseDTO(Marcacao marcacao) {
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();
        dto.setId(marcacao.getId());
        dto.setColaboradorId(marcacao.getColaboradorId());
        dto.setTipo(marcacao.getTipo());
        dto.setDataHora(marcacao.getDataHora());
        dto.setProcessada(marcacao.isProcessada());

        Colaborador colaborador = colaboradorRepository.findById(marcacao.getColaboradorId())
                .orElse(null);
        if (colaborador != null) {
            dto.setNomeColaborador(colaborador.getNome());
        } else {
            dto.setNomeColaborador("Desconhecido");
        }

        return dto;
    }

    public List<MarcacaoResponseDTO> listarTodasMarcacoesComNomes() {
        return marcacaoRepository.findAll().stream()
                .map(this::toMarcacaoResponseDTO)
                .toList();
    }

    public Marcacao atualizarDataHorarioETipoMarcacao(String id, LocalDateTime novaDataHora, TipoMarcacao novoTipo) {
        Marcacao marcacao = marcacaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));

        if (marcacao.isProcessada()) {
            throw new RuntimeException("Não é possível alterar uma marcação processada.");
        }

        marcacao.setDataHora(novaDataHora);
        marcacao.setTipo(novoTipo);

        return marcacaoRepository.save(marcacao);
    }

    public List<MarcacaoResponseDTO> obterMarcacoesPorDataComNomes(LocalDate data) {
        LocalDateTime startOfDay = data.atStartOfDay();
        LocalDateTime endOfDay = data.atTime(LocalTime.MAX);
        List<Marcacao> marcacoes = marcacaoRepository.findByDataHoraBetween(startOfDay, endOfDay);

        return marcacoes.stream().map(marcacao -> {
            MarcacaoResponseDTO dto = new MarcacaoResponseDTO();
            dto.setId(marcacao.getId());
            dto.setColaboradorId(marcacao.getColaboradorId());
            dto.setDataHora(marcacao.getDataHora());
            dto.setTipo(marcacao.getTipo());
            dto.setProcessada(marcacao.isProcessada());

            colaboradorRepository.findById(marcacao.getColaboradorId())
                .ifPresent(colaborador -> dto.setNomeColaborador(colaborador.getNome()));

            return dto;
        }).collect(Collectors.toList());
    }

    public List<MarcacaoResponseDTO> buscarMarcacoesPorNomeColaborador(String nome) {
        List<Colaborador> colaboradores = colaboradorRepository.findByNomeContainingIgnoreCase(nome);
        List<Long> idsColaboradores = colaboradores.stream()
                .map(Colaborador::getId)
                .toList();

        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdIn(idsColaboradores);

        return marcacoes.stream()
                .map(this::toMarcacaoResponseDTO)
                .toList();
    }

    public List<MarcacaoResponseDTO> buscarMarcacoesPorTipo(Marcacao.TipoMarcacao tipo) {
        List<Marcacao> marcacoes = marcacaoRepository.findByTipo(tipo);

        return marcacoes.stream()
                .map(this::toMarcacaoResponseDTO)
                .toList();
    }

    public Marcacao criarMarcacaoComData(Marcacao marcacao, LocalDateTime dataHora) {
        validarMarcacaoDuplicada(marcacao);

        marcacao.setDataHora(dataHora);
        marcacao.setProcessada(false);

        Marcacao novaMarcacao = marcacaoRepository.save(marcacao);
        logger.info("Marcação registrada com sucesso: {}", novaMarcacao);

        MarcacaoLog log = new MarcacaoLog(marcacao.getColaboradorId(), "CRIACAO", marcacao.getTipo());
        marcacaoLogRepository.save(log);

        if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA) {
            calcularTotalTrabalhadoDia(marcacao.getColaboradorId(), dataHora.toLocalDate());
        }

        return novaMarcacao;
    }

    public List<UltimaMarcacaoResumoDTO> ultimasMarcacoesDeHoje(int quantidade) {
        LocalDate hoje = LocalDate.now();

        List<Marcacao> marcacoesHoje = marcacaoRepository.findByDataHoraBetween(
            hoje.atStartOfDay(),
            hoje.plusDays(1).atStartOfDay()
        );

        Map<Long, List<Marcacao>> marcacoesPorColaborador = marcacoesHoje.stream()
            .collect(Collectors.groupingBy(Marcacao::getColaboradorId));

        List<Map.Entry<Long, LocalDateTime>> ultimasMarcacoes = marcacoesPorColaborador.entrySet().stream()
            .map(entry -> {
                LocalDateTime ultimaDataHora = entry.getValue().stream()
                    .map(Marcacao::getDataHora)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.MIN);
                return Map.entry(entry.getKey(), ultimaDataHora);
            })
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(quantidade)
            .collect(Collectors.toList());

        List<UltimaMarcacaoResumoDTO> resultado = new ArrayList<>();

        for (Map.Entry<Long, LocalDateTime> entry : ultimasMarcacoes) {
            Long colaboradorId = entry.getKey();
            Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

            List<Marcacao> marcacoesDoColaborador = marcacoesPorColaborador.get(colaboradorId);

            String entrada = obterHorarioPorTipo(marcacoesDoColaborador, Marcacao.TipoMarcacao.ENTRADA);
            String pausa = obterHorarioPorTipo(marcacoesDoColaborador, Marcacao.TipoMarcacao.PAUSA);
            String retomada = obterHorarioPorTipo(marcacoesDoColaborador, Marcacao.TipoMarcacao.RETOMADA);
            String saida = obterHorarioPorTipo(marcacoesDoColaborador, Marcacao.TipoMarcacao.SAIDA);

            String total = calcularTotalHoras(marcacoesDoColaborador);

            resultado.add(new UltimaMarcacaoResumoDTO(
                colaborador.getNome(),
                entrada,
                pausa,
                retomada,
                saida,
                total
            ));
        }

        return resultado;
    }

    private String obterHorarioPorTipo(List<Marcacao> marcacoes, Marcacao.TipoMarcacao tipo) {
        return marcacoes.stream()
            .filter(m -> m.getTipo() == tipo)
            .map(m -> m.getDataHora().toLocalTime().toString())
            .findFirst()
            .orElse("-");
    }

    private String calcularTotalHoras(List<Marcacao> marcacoes) {
        Optional<LocalDate> dataOptional = marcacoes.stream().map(m -> m.getDataHora().toLocalDate()).findFirst();
        if (dataOptional.isEmpty()) return "0h";

        Optional<Marcacao> entradaOpt = marcacoes.stream().filter(m -> m.getTipo() == Marcacao.TipoMarcacao.ENTRADA).findFirst();
        Optional<Marcacao> saidaOpt = marcacoes.stream().filter(m -> m.getTipo() == Marcacao.TipoMarcacao.SAIDA).findFirst();

        if (entradaOpt.isPresent() && saidaOpt.isPresent()) {
            long horas = java.time.Duration.between(entradaOpt.get().getDataHora(), saidaOpt.get().getDataHora()).toHours();
            return horas + "h";
        }
        return "0h";
    }

    public List<MarcacoesPorDiaDTO> marcacoesPorDia(LocalDate data) {
        if (data == null) {
            data = LocalDate.now();
        }

        List<Marcacao> marcacoes = marcacaoRepository.findByDataHoraBetween(
            data.atStartOfDay(),
            data.plusDays(1).atStartOfDay()
        );

        Map<Long, List<Marcacao>> marcacoesPorColaborador = marcacoes.stream()
            .collect(Collectors.groupingBy(Marcacao::getColaboradorId));

        List<Colaborador> colaboradores = colaboradorRepository.findAll();

        List<MarcacoesPorDiaDTO> resultado = new ArrayList<>();

        for (Colaborador colaborador : colaboradores) {
            String status = "PRESENTE";

            boolean temFolga = folgaRepository.existsByColaboradorIdAndData(colaborador.getId(), data);
            if (temFolga) {
                status = "FOLGA";
            }

            boolean emFerias = feriasRepository.existsByColaboradorIdAndData(colaborador.getId(), data);
            if (emFerias) {
                status = "FÉRIAS";
            }

            boolean temFalta = faltaRepository.existsByColaboradorIdAndData(colaborador.getId(), data);
            if (temFalta) {
                status = "FALTA";
            }

            List<MarcacaoResumoDTO> marcacoesResumo = new ArrayList<>();

            if (status.equals("PRESENTE")) {
                List<Marcacao> marcacoesDoColaborador = marcacoesPorColaborador.getOrDefault(colaborador.getId(), new ArrayList<>());

                for (Marcacao.TipoMarcacao tipo : Marcacao.TipoMarcacao.values()) {
                    String horario = marcacoesDoColaborador.stream()
                        .filter(m -> m.getTipo() == tipo)
                        .map(m -> m.getDataHora().toLocalTime().toString())
                        .findFirst()
                        .orElse("--:--");

                    marcacoesResumo.add(new MarcacaoResumoDTO(tipo.name(), horario));
                }
            } else {
                marcacoesResumo.add(new MarcacaoResumoDTO(status, "--:--"));
            }

            resultado.add(new MarcacoesPorDiaDTO(
                colaborador.getNome(),
                status,
                marcacoesResumo
            ));
        }

        return resultado;
    }
}
