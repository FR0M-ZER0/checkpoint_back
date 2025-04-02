package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.*;
import com.fromzero.checkpoint.repositories.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class VerificacaoPontoService {
    private final CargaDiariaRepository cargaDiariaRepository;
    private final MarcacaoRepository marcacaoRepository;
    private final NotificacaoService notificacaoService;
    private final ColaboradorRepository colaboradorRepository;

    public VerificacaoPontoService(CargaDiariaRepository cargaDiariaRepository,
                                 MarcacaoRepository marcacaoRepository,
                                 NotificacaoService notificacaoService,
                                 ColaboradorRepository colaboradorRepository) {
        this.cargaDiariaRepository = cargaDiariaRepository;
        this.marcacaoRepository = marcacaoRepository;
        this.notificacaoService = notificacaoService;
        this.colaboradorRepository = colaboradorRepository;
    }

    @Scheduled(cron = "0 * * * * *") // Executa a cada minuto (ajuste conforme necessidade)
    public void verificarSaidasNaoRegistradas() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();
        
        // Busca todas as cargas diárias
        List<CargaDiaria> cargas = cargaDiariaRepository.findAll();
        
        for (CargaDiaria carga : cargas) {
            if (carga.getFim() != null && carga.getJornada() != null && carga.getJornada().getColaborador() != null) {
                LocalDateTime horarioSaida = hoje.atTime(carga.getFim());
                LocalDateTime horarioVerificacao = horarioSaida.plusHours(1);
                
                // Verifica se já passou 1 hora do horário de saída
                if (agora.isAfter(horarioVerificacao) && agora.isBefore(horarioVerificacao.plusMinutes(1))) {
                    verificarPontoColaborador(carga.getJornada().getColaborador(), hoje);
                }
            }
        }
    }
    
    private void verificarPontoColaborador(Colaborador colaborador, LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);
        
        boolean possuiSaida = marcacaoRepository
            .findByColaboradorIdAndDataHoraBetween(colaborador.getId(), inicioDia, fimDia)
            .stream()
            .anyMatch(m -> m.getTipo() == Marcacao.TipoMarcacao.SAIDA);
            
        if (!possuiSaida) {
            String mensagem = "Você esqueceu de bater o ponto de saída hoje. Deseja registrar hora extra?";
            notificacaoService.criaNotificacao(
                mensagem, 
                Notificacao.NotificacaoTipo.ponto, 
                colaborador);
        }
    }
}