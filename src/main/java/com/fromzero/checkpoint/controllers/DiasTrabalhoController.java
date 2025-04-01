package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.entities.Folga;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dias-trabalho")
public class DiasTrabalhoController {

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private FeriasRepository feriasRepository;

    @Autowired
    private FolgaRepository folgaRepository;

    @GetMapping("/{colaboradorId}")
    public Map<LocalDate, String> getDiasTrabalho(@PathVariable Long colaboradorId) {
        Map<LocalDate, String> diasTrabalho = new TreeMap<>(Comparator.reverseOrder());

        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorId(colaboradorId);

        Map<LocalDate, List<Marcacao>> marcacoesPorDia = marcacoes.stream()
                .collect(Collectors.groupingBy(m -> m.getDataHora().toLocalDate()));

        marcacoesPorDia.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 4)
                .forEach(entry -> diasTrabalho.put(entry.getKey(), "normal"));

        List<Falta> faltas = faltaRepository.findByColaboradorId(colaboradorId);

        for (Falta falta : faltas) {
            LocalDate diaFalta = falta.getCriadoEm().toLocalDate();
            diasTrabalho.put(diaFalta, "falta");
        }

        List<Folga> folgas = folgaRepository.findByColaboradorId(colaboradorId);

        for (Folga folga : folgas) {
            diasTrabalho.put(folga.getData(), "folga");
        }

        List<Ferias> feriasList = feriasRepository.findByColaboradorId(colaboradorId);

        for (Ferias ferias : feriasList) {
            LocalDate inicio = ferias.getDataInicio();
            LocalDate fim = ferias.getDataFim();
            while (!inicio.isAfter(fim)) {
                diasTrabalho.put(inicio, "ferias");
                inicio = inicio.plusDays(1);
            }
        }

        return diasTrabalho;
    }

    private double calcularHorasTrabalhadas(List<Marcacao> marcacoes) {
        if (marcacoes.size() < 2) return 0;
    
        List<Marcacao> sortedMarcacoes = marcacoes.stream()
                .sorted(Comparator.comparing(Marcacao::getDataHora))
                .collect(Collectors.toList());
    
        double totalHoras = 0;
        for (int i = 0; i < sortedMarcacoes.size() - 1; i += 2) {
            totalHoras += java.time.Duration.between(
                    sortedMarcacoes.get(i).getDataHora(),
                    sortedMarcacoes.get(i + 1).getDataHora()
            ).toHours();
        }
        return totalHoras;
    }    

    @GetMapping("/{colaboradorId}/resumo")
    public Map<String, Object> getResumoTrabalho(@PathVariable Long colaboradorId) {
        Map<String, Object> resumo = new TreeMap<>();

        long totalFaltas = faltaRepository.countByColaboradorId(colaboradorId);
        resumo.put("totalFaltas", totalFaltas);

        long totalFolgas = folgaRepository.countByColaboradorId(colaboradorId);
        resumo.put("totalFolgas", totalFolgas);

        long totalFerias = feriasRepository.findByColaboradorId(colaboradorId)
                .stream()
                .mapToLong(f -> java.time.temporal.ChronoUnit.DAYS.between(f.getDataInicio(), f.getDataFim()) + 1)
                .sum();
        resumo.put("totalFerias", totalFerias);

        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorId(colaboradorId);
        Map<LocalDate, List<Marcacao>> marcacoesPorDia = marcacoes.stream()
                .collect(Collectors.groupingBy(m -> m.getDataHora().toLocalDate()));

        double totalHorasTrabalhadas = marcacoesPorDia.values().stream()
                .mapToDouble(this::calcularHorasTrabalhadas)
                .sum();

        resumo.put("totalHorasTrabalhadas", totalHorasTrabalhadas);

        return resumo;
    }

    @GetMapping("/{colaboradorId}/{data}")
    public ResponseEntity<Map<String, Object>> getDiaTrabalho(@PathVariable Long colaboradorId, @PathVariable String data) {
        LocalDate date = LocalDate.parse(data);
        Map<String, Object> response = new HashMap<>();

        boolean estaDeFerias = feriasRepository.findByColaboradorId(colaboradorId)
                .stream()
                .anyMatch(f -> !date.isBefore(f.getDataInicio()) && !date.isAfter(f.getDataFim()));
        if (estaDeFerias) {
            response.put("tipo", "ferias");
            return ResponseEntity.ok(response);
        }

        boolean estaDeFolga = folgaRepository.findByColaboradorId(colaboradorId)
                .stream()
                .anyMatch(f -> f.getData().equals(date));
        if (estaDeFolga) {
            response.put("tipo", "folga");
            return ResponseEntity.ok(response);
        }

        Falta falta = faltaRepository.findByColaboradorId(colaboradorId)
                .stream()
                .filter(f -> f.getCriadoEm().toLocalDate().equals(date))
                .findFirst()
                .orElse(null);
        if (falta != null) {
            response.put("tipo", "falta");
            response.put("detalhes", Map.of("tipoFalta", falta.getTipo().name()));
            return ResponseEntity.ok(response);
        }

        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorId(colaboradorId)
                .stream()
                .filter(m -> m.getDataHora().toLocalDate().equals(date))
                .collect(Collectors.toList());

        if (marcacoes.size() == 4) {
            response.put("tipo", "normal");
            response.put("marcacoes", marcacoes);
            return ResponseEntity.ok(response);
        }

        response.put("tipo", "desconhecido");
        return ResponseEntity.ok(response);
    }
}
