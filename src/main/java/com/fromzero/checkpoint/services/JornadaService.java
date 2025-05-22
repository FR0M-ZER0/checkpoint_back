package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.Jornada;
import com.fromzero.checkpoint.entities.ColaboradorJornada;
import com.fromzero.checkpoint.repositories.ColaboradorJornadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
public class JornadaService {
    private static final Logger log = LoggerFactory.getLogger(JornadaService.class);

    @Autowired
    private ColaboradorJornadaRepository colaboradorJornadaRepository;

    public Optional<Jornada> findActiveJornadaForColaborador(Long colaboradorId, LocalDate date) {
        log.debug("Buscando jornada ativa para colab {} na data {}", colaboradorId, date);
        List<ColaboradorJornada> cjList = colaboradorJornadaRepository
            .findActiveJornadaForColaboradorAtDate(colaboradorId, date);
        if (cjList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cjList.get(0).getJornada()); // Pega a primeira da lista (mais recente)
    }

    public boolean isDiaDeDescansoEscala(Long colaboradorId, LocalDate date) {
        Optional<Jornada> jornadaOpt = findActiveJornadaForColaborador(colaboradorId, date);
        if (jornadaOpt.isPresent()) {
            Jornada jornada = jornadaOpt.get();
            String escala = jornada.getEscala();
            DayOfWeek diaDaSemana = date.getDayOfWeek();
            log.debug("Verificando descanso: Colab {}, Data {}, Escala {}, DiaSemana {}", colaboradorId, date, escala, diaDaSemana);
            if ("6x1".equalsIgnoreCase(escala) && diaDaSemana == DayOfWeek.SUNDAY) return true;
            if ("5x2".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) return true;
            if ("4x3".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.FRIDAY || diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) return true;
        }
        return false;
    }
}