package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Falta.TipoFalta;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.foreign.Linker.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Service
public class RegistroAusenciaAutomaticaService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Scheduled(cron = "0 5 0 * * ?") // Executa diariamente à meia-noite
    public void verificarAusenciasDiarias() {
        LocalDate hoje = LocalDate.now();

        List<Colaborador> colaboradores = colaboradorRepository.findAll();

        for (Colaborador colaborador : colaboradores) {
            boolean temMarcacaoHoje = possuiMarcacaoHoje(colaborador.getId(), hoje);

            if (!temMarcacaoHoje) {
                registrarAusencia(colaborador, hoje);
            }
        }
    }

    private boolean possuiMarcacaoHoje(Long colaboradorId, LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime finalDoDia = data.plusDays(1).atStartOfDay();

        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                colaboradorId, inicioDia, finalDoDia);

        return !marcacoes.isEmpty();
    }

    private void registrarAusencia(Colaborador colaborador, LocalDate data) {
        Optional<Falta> faltaExistente = faltaRepository.findByColaboradorIdAndCriadoEmOnDate(
                colaborador.getId(), data);

        if (faltaExistente.isPresent()) {
            return;
    }

        Falta falta = new Falta();
        falta.setTipo(TipoFalta.Ausencia);
        falta.setColaborador(colaborador);
        falta.setJustificado(false);
        falta.setCriadoEm(LocalDateTime.now());

        faltaRepository.save(falta);
    } 
}
