package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.entities.Folga;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.SolicitacaoFerias;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAbonoFaltaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFeriasRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;
import com.fromzero.checkpoint.services.JornadaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/dias-trabalho")
public class DiasTrabalhoController {

    private static final Logger log = LoggerFactory.getLogger(DiasTrabalhoController.class);

    @Autowired
    private JornadaService jornadaService;

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private FeriasRepository feriasRepository;

    @Autowired
    private FolgaRepository folgaRepository;

    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository;

    @Autowired
    private SolicitacaoFeriasRepository solicitacaoFeriasRepository;

    @Autowired
    private SolicitacaoAbonoFaltaRepository solicitacaoAbonoFaltaRepository;

@GetMapping("/{colaboradorId}")
public ResponseEntity<Map<String, String>> getDiasTrabalho(
        @PathVariable Long colaboradorId,
        @RequestParam(required = false) Integer ano) {

    int anoParaProcessar = (ano != null) ? ano : LocalDate.now().getYear();
    log.info("CONTROLLER: Solicitado calendário para Colaborador ID: {} no Ano: {}", colaboradorId, anoParaProcessar);

    Map<String, String> diasComStatus = new TreeMap<>(); // Para ordenar as datas "AAAA-MM-DD"

    LocalDate inicioDoAno = LocalDate.of(anoParaProcessar, 1, 1);
    LocalDate fimDoAno = LocalDate.of(anoParaProcessar, 12, 31);
    LocalDateTime inicioDoAnoTime = inicioDoAno.atStartOfDay();
    // Para queries que usam '<' no final do range (ex: criadoEm < dataFinalMaisUmDia)
    LocalDateTime inicioDoProximoAnoTime = inicioDoAno.plusYears(1).atStartOfDay(); 

    // 1. BUSCAR TODOS OS EVENTOS RELEVANTES PARA O ANO
    // Certifique-se que os métodos nos repositórios estão corretos e implementados
    List<Marcacao> todasMarcacoesDoAno = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
        colaboradorId, inicioDoAnoTime, fimDoAno.atTime(LocalTime.MAX) // Pega até o fim do último dia
    );
    List<Falta> todasFaltasDoAno = faltaRepository.findByColaboradorIdAndCriadoEmBetweenYearRange(
        colaboradorId, inicioDoAnoTime, inicioDoProximoAnoTime
    );
    List<Folga> todasFolgasDoAno = folgaRepository.findByColaboradorIdAndDataBetween(
        colaboradorId, inicioDoAno, fimDoAno
    );
    List<SolicitacaoFerias> todasFeriasAprovadasDoAno = solicitacaoFeriasRepository
        .findAprovadasByColaboradorIdOverlappingYear(colaboradorId, inicioDoAno, fimDoAno);

    // Mapear para busca rápida dentro do loop diário
    Map<LocalDate, Boolean> mapaDiasComMarcacao = todasMarcacoesDoAno.stream()
        .map(m -> m.getDataHora().toLocalDate())
        .distinct()
        .collect(Collectors.toMap(date -> date, date -> true));

    Map<LocalDate, Falta> mapaFaltas = todasFaltasDoAno.stream()
        .collect(Collectors.toMap(
            f -> f.getCriadoEm().toLocalDate(), // Chave é a data da falta
            falta -> falta,
            (f1, f2) -> f1 // Em caso de mais de uma falta no mesmo dia, pega a primeira
        ));

    Map<LocalDate, Folga> mapaFolgas = todasFolgasDoAno.stream()
        .collect(Collectors.toMap(
            Folga::getData, 
            folga -> folga,
            (f1, f2) -> f1 // Em caso de mais de uma folga no mesmo dia
        ));
    
    Map<LocalDate, SolicitacaoFerias> mapaFerias = new HashMap<>();
    for (SolicitacaoFerias ferias : todasFeriasAprovadasDoAno) {
        for (LocalDate d = ferias.getDataInicio(); !d.isAfter(ferias.getDataFim()); d = d.plusDays(1)) {
            if (d.getYear() == anoParaProcessar) {
                mapaFerias.put(d, ferias);
            }
        }
    }

    // 2. ITERAR POR CADA DIA DO ANO E DEFINIR STATUS COM PRIORIDADE
    for (LocalDate dataAtual = inicioDoAno; !dataAtual.isAfter(fimDoAno); dataAtual = dataAtual.plusDays(1)) {
        String statusFinalDoDia = null;

        if (mapaFerias.containsKey(dataAtual)) {
            statusFinalDoDia = "ferias";
        } else if (mapaFolgas.containsKey(dataAtual)) {
            statusFinalDoDia = "folga";
        } else if (jornadaService.isDiaDeDescansoEscala(colaboradorId, dataAtual)) {
            statusFinalDoDia = "DESCANSO_ESCALA"; // Status novo
        } else if (mapaFaltas.containsKey(dataAtual)) {
            statusFinalDoDia = "falta";
        } else if (mapaDiasComMarcacao.getOrDefault(dataAtual, false)) {
            statusFinalDoDia = "normal";
        }
        
        if (statusFinalDoDia != null) {
            diasComStatus.put(dataAtual.toString(), statusFinalDoDia);
        }
    }
    return ResponseEntity.ok(diasComStatus);
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
    
            solicitacaoFeriasRepository.findByColaboradorId(colaboradorId).stream()
                    .filter(s -> !date.isBefore(s.getDataInicio()) && !date.isAfter(s.getDataFim()))
                    .findFirst()
                    .ifPresent(s -> response.put("detalhes", Map.of(
                            "observacao", s.getObservacao(),
                            "comentarioGestor", s.getComentarioGestor()
                    )));
            return ResponseEntity.ok(response);
        }
    
        boolean estaDeFolga = folgaRepository.findByColaboradorId(colaboradorId)
                .stream()
                .anyMatch(f -> f.getData().equals(date));
        if (estaDeFolga) {
            response.put("tipo", "folga");
    
            solicitacaoFolgaRepository.findByColaboradorId(colaboradorId).stream()
                    .filter(s -> s.getSolFolData().equals(date))
                    .findFirst()
                    .ifPresent(s -> response.put("detalhes", Map.of(
                            "observacao", s.getSolFolObservacao()
                    )));
            return ResponseEntity.ok(response);
        }
    
        Falta falta = faltaRepository.findByColaboradorId(colaboradorId)
                .stream()
                .filter(f -> f.getCriadoEm().toLocalDate().equals(date))
                .findFirst()
                .orElse(null);
        if (falta != null) {
            response.put("tipo", "falta");
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("tipoFalta", falta.getTipo().name());
    
            solicitacaoAbonoFaltaRepository.findByFaltaId(falta.getId()).ifPresent(s -> {
                detalhes.put("motivo", s.getMotivo());
                detalhes.put("justificativa", s.getJustificativa());
            });
    
            response.put("detalhes", detalhes);
            return ResponseEntity.ok(response);
        }
    
        List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorId(colaboradorId)
                .stream()
                .filter(m -> m.getDataHora().toLocalDate().equals(date))
                .collect(Collectors.toList());
    
        if (!marcacoes.isEmpty()) {
            response.put("tipo", "normal");
            response.put("marcacoes", marcacoes);
            return ResponseEntity.ok(response);
        }
    
        response.put("tipo", "desconhecido");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{colaboradorId}/filtro/{tipo}")
    public ResponseEntity<Map<LocalDate, String>> getDiasPorTipo(
            @PathVariable Long colaboradorId, 
            @PathVariable String tipo) {
    
        Map<LocalDate, String> diasFiltrados = new TreeMap<>(Comparator.reverseOrder());
    
        switch (tipo.toLowerCase()) {
            case "folga":
                folgaRepository.findByColaboradorId(colaboradorId)
                        .forEach(folga -> diasFiltrados.put(folga.getData(), "folga"));
                break;
            
            case "falta":
                faltaRepository.findByColaboradorId(colaboradorId)
                        .forEach(falta -> diasFiltrados.put(falta.getCriadoEm().toLocalDate(), "falta"));
                break;
    
            case "ferias":
                feriasRepository.findByColaboradorId(colaboradorId)
                        .forEach(ferias -> {
                            LocalDate inicio = ferias.getDataInicio();
                            LocalDate fim = ferias.getDataFim();
                            while (!inicio.isAfter(fim)) {
                                diasFiltrados.put(inicio, "ferias");
                                inicio = inicio.plusDays(1);
                            }
                        });
                break;
    
            case "marcacao":
                marcacaoRepository.findByColaboradorId(colaboradorId)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getDataHora().toLocalDate()))
                        .forEach((data, marcacoes) -> diasFiltrados.put(data, "normal"));
                break;
    
            default:
                return ResponseEntity.badRequest().body(null);
        }
    
        return ResponseEntity.ok(diasFiltrados);
    }
}
