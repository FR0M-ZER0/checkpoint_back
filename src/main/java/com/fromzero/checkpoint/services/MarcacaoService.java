package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.MarcacaoLog;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Gestor;
import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.entities.HorasExtras.Status;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.MarcacaoLogRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.services.HorasExtrasService;
import com.fromzero.checkpoint.services.NotificacaoService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@EnableScheduling
@Service
public class MarcacaoService {

    private static final Logger logger = LoggerFactory.getLogger(MarcacaoService.class);

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Autowired
    private MarcacaoLogRepository marcacaoLogRepository;

    @Autowired
    private HorasExtrasService horasExtrasService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private JavaMailSender mailSender;

    public List<Marcacao> listarMarcacoes() {
        return marcacaoRepository.findAll();
    }

    public Optional<Marcacao> buscarMarcacaoPorId(String id) {
        return marcacaoRepository.findById(id);
    }

    public Marcacao criarMarcacao(Marcacao marcacao) {
        validarMarcacaoDuplicada(marcacao);

        marcacao.setDataHora(LocalDateTime.now());
        marcacao.setProcessada(false);

        Marcacao novaMarcacao = marcacaoRepository.save(marcacao);
        logger.info("Marcação registrada com sucesso: {}", novaMarcacao);

        MarcacaoLog log = new MarcacaoLog(marcacao.getColaboradorId(), "CRIACAO", marcacao.getTipo());
        marcacaoLogRepository.save(log);

        if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA) {
            LocalDate data = marcacao.getDataHora().toLocalDate();
            processarTrabalhoDiario(marcacao.getColaboradorId(), data);
        }

        return novaMarcacao;
    }

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

        MarcacaoLog log = new MarcacaoLog(marcacao.getColaboradorId(), "DELECAO", marcacao.getTipo());
        marcacaoLogRepository.save(log);
    }

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

    public List<Marcacao> obterMarcacoesDoDia(Long colaboradorId, LocalDate data) {
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        return marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDoDia, fimDoDia);
    }

    public List<Marcacao> obterTodasMarcacoesPorColaborador(Long colaboradorId) {
        return marcacaoRepository.findByColaboradorId(colaboradorId);
    }

    public List<Marcacao> obterMarcacoesPorData(Long colaboradorId, LocalDate data) {
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        return marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDoDia, fimDoDia);
    }

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
        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                colaboradorId,
                dia.atStartOfDay(),
                dia.plusDays(1).atStartOfDay()
        );

        LocalDateTime entrada = null;
        Duration totalTrabalhado = Duration.ZERO;

        for (Marcacao marcacao : marcacoes) {
            if (marcacao.getTipo() == Marcacao.TipoMarcacao.ENTRADA) {
                entrada = marcacao.getDataHora();
            } else if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA && entrada != null) {
                totalTrabalhado = totalTrabalhado.plus(Duration.between(entrada, marcacao.getDataHora()));
                entrada = null;
            }
        }

        long horas = totalTrabalhado.toHours();
        long minutos = totalTrabalhado.toMinutesPart();

        return String.format("%02dh:%02dmin", horas, minutos);
    }

    @Transactional
    public void processarTrabalhoDiario(Long colaboradorId, LocalDate data) {
        List<Marcacao> marcacoes = obterMarcacoesPorData(colaboradorId, data);

        if (marcacoes.isEmpty()) return;

        marcacoes.sort(Comparator.comparing(Marcacao::getDataHora));

        Duration tempoTrabalhado = Duration.ZERO;
        LocalDateTime inicioTrabalho = null;
        boolean emPausa = false;
        boolean marcouSaida = false;

        for (Marcacao marcacao : marcacoes) {
            if (marcacao.getTipo() == Marcacao.TipoMarcacao.ENTRADA) {
                inicioTrabalho = marcacao.getDataHora();
            } else if (marcacao.getTipo() == Marcacao.TipoMarcacao.PAUSA && inicioTrabalho != null) {
                tempoTrabalhado = tempoTrabalhado.plus(Duration.between(inicioTrabalho, marcacao.getDataHora()));
                emPausa = true;
            } else if (marcacao.getTipo() == Marcacao.TipoMarcacao.RETOMADA && emPausa) {
                inicioTrabalho = marcacao.getDataHora();
                emPausa = false;
            } else if (marcacao.getTipo() == Marcacao.TipoMarcacao.SAIDA && inicioTrabalho != null) {
                tempoTrabalhado = tempoTrabalhado.plus(Duration.between(inicioTrabalho, marcacao.getDataHora()));
                inicioTrabalho = null;
                marcouSaida = true;
            }
        }

        Duration jornadaNormal = Duration.ofHours(8);
        Duration jornadaMaxima = Duration.ofHours(11);

        if (!marcouSaida) {
            LocalDateTime entradaInicial = marcacoes.get(0).getDataHora();
            if (Duration.between(entradaInicial, LocalDateTime.now()).compareTo(jornadaMaxima) >= 0) {
                Marcacao saidaAutomatica = new Marcacao();
                saidaAutomatica.setColaboradorId(colaboradorId);
                saidaAutomatica.setDataHora(entradaInicial.plusHours(11));
                saidaAutomatica.setTipo(Marcacao.TipoMarcacao.SAIDA);
                saidaAutomatica.setProcessada(true);
                criarMarcacao(saidaAutomatica);

                registrarHorasExtras(colaboradorId, Duration.ofHours(3));
                criarNotificacao(colaboradorId, "Saída automática registrada após 11h de trabalho. 3h de horas extras adicionadas.");
            }
            return;
        }

        if (tempoTrabalhado.compareTo(jornadaNormal) > 0) {
            Duration tempoExtra = tempoTrabalhado.minus(jornadaNormal);
            registrarHorasExtras(colaboradorId, tempoExtra);
            criarNotificacao(colaboradorId, "Horas extras registradas: " + tempoExtra.toHours() + "h " + tempoExtra.toMinutesPart() + "min.");
        }
    }

    private void registrarHorasExtras(Long colaboradorId, Duration saldo) {
        if (saldo.isZero() || saldo.isNegative()) return;

        double horasDecimais = saldo.toMinutes() / 60.0;
        String saldoFormatado = String.format("%.2fh", horasDecimais).replace(",", ".").replaceAll("\\.0+h", "h"); 
        
        
        HorasExtras horasExtras = new HorasExtras(saldoFormatado, Status.Aprovado, colaboradorId);
        horasExtras.setJustificativa("");
        horasExtrasService.salvarHorasExtras(horasExtras);

        enviarEmailParaTodosGestores(colaboradorId, saldoFormatado);
    }

    private void criarNotificacao(Long colaboradorId, String mensagem) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        notificacaoService.criaNotificacao(mensagem, NotificacaoTipo.horasExtras, colaborador);
    }

    @Scheduled(fixedRate = 60000) // Executa a cada 1 minuto
    public void verificarEForcarSaidaAposTresHorasExtras() {
        List<Colaborador> colaboradores = colaboradorRepository.findAll();
        for (Colaborador colaborador : colaboradores) {
            LocalDate hoje = LocalDate.now();
            List<Marcacao> marcacoesHoje = obterMarcacoesDoDia(colaborador.getId(), hoje);

            if (marcacoesHoje.isEmpty()) continue;

            // Verifica se tem ENTRADA hoje
            boolean temEntrada = marcacoesHoje.stream()
                .anyMatch(m -> m.getTipo() == Marcacao.TipoMarcacao.ENTRADA);

            if (!temEntrada) {
                // Não tem entrada -> usuário ainda não voltou => IGNORA
                continue;
            }

            // Verifica se já tem SAÍDA registrada
            boolean jaSaiu = marcacoesHoje.stream()
                .anyMatch(m -> m.getTipo() == Marcacao.TipoMarcacao.SAIDA);

            if (jaSaiu) {
                // Já tem saída registrada, então espera nova entrada
                continue;
            }

            Duration tempoTrabalhado = calcularTempoTrabalhado(marcacoesHoje);

            Duration jornadaNormal = Duration.ofHours(8);
            Duration limiteExtras = Duration.ofHours(3);

            if (tempoTrabalhado.compareTo(jornadaNormal.plus(limiteExtras)) > 0) {
                LocalDateTime entrada = marcacoesHoje.get(0).getDataHora();
                LocalDateTime horarioSaidaPermitido = entrada.plusHours(8).plusHours(3);

                Marcacao saidaAutomatica = new Marcacao();
                saidaAutomatica.setColaboradorId(colaborador.getId());
                saidaAutomatica.setDataHora(horarioSaidaPermitido);
                saidaAutomatica.setTipo(Marcacao.TipoMarcacao.SAIDA);
                saidaAutomatica.setProcessada(true);

                criarMarcacao(saidaAutomatica);
                criarNotificacao(colaborador.getId(), "Saída automática registrada após atingir o limite de 3h de horas extras.");
            }
        }
    }


    private Duration calcularTempoTrabalhado(List<Marcacao> marcacoes) {
        marcacoes.sort(Comparator.comparing(Marcacao::getDataHora));
        Duration tempoTotal = Duration.ZERO;
        LocalDateTime inicio = null;

        for (Marcacao m : marcacoes) {
            if (m.getTipo() == Marcacao.TipoMarcacao.ENTRADA) {
                inicio = m.getDataHora();
            } else if (m.getTipo() == Marcacao.TipoMarcacao.PAUSA && inicio != null) {
                tempoTotal = tempoTotal.plus(Duration.between(inicio, m.getDataHora()));
                inicio = null;
            } else if (m.getTipo() == Marcacao.TipoMarcacao.RETOMADA) {
                inicio = m.getDataHora();
            }
        }

        if (inicio != null) {
            tempoTotal = tempoTotal.plus(Duration.between(inicio, LocalDateTime.now()));
        }

        return tempoTotal;
    }
    private void enviarEmailParaTodosGestores(Long colaboradorId, String saldoHoras) {
    Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

    List<Gestor> gestores = gestorRepository.findAll();

    if (gestores.isEmpty()) {
        logger.warn("Nenhum gestor encontrado para enviar notificação de horas extras.");
        return;
    }

    for (Gestor gestor : gestores) {
        if (gestor.getEmail() != null && !gestor.getEmail().isEmpty()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(gestor.getEmail());
                message.setSubject("Novo Lançamento de Horas Extras - " + colaborador.getNome());
                message.setText("Olá " + gestor.getNome() + ",\n\n" +
                                "O colaborador " + colaborador.getNome() + " registrou " + saldoHoras + " de horas extras automaticamente.\n\n" +
                                "Por favor, avalie e tome as ações necessárias.\n\n" +
                                "Atenciosamente,\nEquipe CheckPoint");

                mailSender.send(message);
            } catch (Exception e) {
                logger.error("Erro ao enviar e-mail para o gestor: " + gestor.getEmail(), e);
            }
        }
    }
}

}
