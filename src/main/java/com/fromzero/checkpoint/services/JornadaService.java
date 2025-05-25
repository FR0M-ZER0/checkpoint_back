package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.dto.JornadaAtivaDTO; // << CRIAR ESTE DTO
import com.fromzero.checkpoint.entities.ColaboradorJornada;
import com.fromzero.checkpoint.entities.Jornada;
import com.fromzero.checkpoint.entities.TipoJornada; // << IMPORTAR TipoJornada
import com.fromzero.checkpoint.repositories.ColaboradorJornadaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime; // << IMPORTAR LocalTime
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JornadaService {

    private static final Logger log = LoggerFactory.getLogger(JornadaService.class);

    @Autowired
    private ColaboradorJornadaRepository colaboradorJornadaRepository;

    public Optional<Jornada> findActiveJornadaForColaborador(Long colaboradorId, LocalDate date) {
        log.debug("JornadaService: Buscando jornada ativa para colaborador ID {} na data {}", colaboradorId, date);
        List<ColaboradorJornada> cjList = colaboradorJornadaRepository
            .findActiveJornadaForColaboradorAtDate(colaboradorId, date);

        if (cjList.isEmpty()) {
            log.debug("JornadaService: Nenhuma ColaboradorJornada ativa encontrada para colab {} na data {}", colaboradorId, date);
            return Optional.empty();
        }
        ColaboradorJornada cjAtiva = cjList.get(0);
        log.debug("JornadaService: ColaboradorJornada ativa encontrada: ID {}, Jornada ID: {}", cjAtiva.getId(), cjAtiva.getJornada().getId());
        return Optional.of(cjAtiva.getJornada());
    }

    public Map<LocalDate, Jornada> getActiveJornadasMapForYear(Long colaboradorId, int year) {
        LocalDate startDateOfYear = LocalDate.of(year, 1, 1);
        LocalDate endDateOfYear = LocalDate.of(year, 12, 31);
        
        log.debug("JornadaService: Buscando todas as ColaboradorJornada relevantes para colab {} no ano {}", colaboradorId, year);
        List<ColaboradorJornada> cjList = colaboradorJornadaRepository
            .findAllRelevantForColaboradorInYear(colaboradorId, startDateOfYear, endDateOfYear);
        
        Map<LocalDate, Jornada> dailyJornadaMap = new HashMap<>();
        
        cjList.sort(Comparator.comparing(ColaboradorJornada::getDataInicio, Comparator.nullsLast(Comparator.reverseOrder())));

        for (ColaboradorJornada cj : cjList) {
            if (cj.getDataInicio() == null) {
                log.warn("JornadaService: ColaboradorJornada ID {} sem data de início para colab {}. Pulando.", cj.getId(), colaboradorId);
                continue;
            }

            LocalDate periodStart = cj.getDataInicio().isBefore(startDateOfYear) ? startDateOfYear : cj.getDataInicio();
            LocalDate periodEnd = (cj.getDataFim() == null || cj.getDataFim().isAfter(endDateOfYear)) ? endDateOfYear : cj.getDataFim();
            
            for (LocalDate date = periodStart; !date.isAfter(periodEnd); date = date.plusDays(1)) {
                dailyJornadaMap.putIfAbsent(date, cj.getJornada());
            }
        }
        log.info("JornadaService: Mapa de jornadas diárias para colab {} no ano {} criado com {} entradas.", colaboradorId, year, dailyJornadaMap.size());
        return dailyJornadaMap;
    }

    public boolean isDiaDeDescansoEscala(Long colaboradorId, LocalDate date) {
        Optional<Jornada> jornadaOpt = findActiveJornadaForColaborador(colaboradorId, date);
        if (jornadaOpt.isPresent()) {
            Jornada jornada = jornadaOpt.get();
            String escala = jornada.getEscala(); // Assume que Jornada tem getEscala() (Lombok @Data)
            DayOfWeek diaDaSemana = date.getDayOfWeek();
            log.debug("JornadaService - isDiaDeDescansoEscala: Verificando Colab {}, Data {}, Escala {}, DiaSemana {}", colaboradorId, date, escala, diaDaSemana);

            if (escala == null) return false;

            if ("6x1".equalsIgnoreCase(escala) && diaDaSemana == DayOfWeek.SUNDAY) return true;
            if ("5x2".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) return true;
            if ("4x3".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.FRIDAY || diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) return true;
        }
        return false;
    }

    public Optional<JornadaAtivaDTO> getDetalhesJornadaAtiva(Long colaboradorId) {
        log.debug("JornadaService: Buscando detalhes da jornada ativa para colaborador ID {}", colaboradorId);
        Optional<Jornada> jornadaOpt = findActiveJornadaForColaborador(colaboradorId, LocalDate.now());
        return jornadaOpt.map(j -> new JornadaAtivaDTO(
            j.getEscala(),         // Assume que Jornada tem getEscala()
            j.getTipoJornada(),    // Assume que Jornada tem getTipoJornada()
            j.getCargaHoraria(),   // Assume que Jornada tem getCargaHoraria()
            j.getInicioPrevisto()  // Assume que Jornada tem getInicioPrevisto()
        ));
    }
}