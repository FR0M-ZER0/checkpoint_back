package com.fromzero.checkpoint.controllers;

// Imports de Entidades
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Folga;
import com.fromzero.checkpoint.entities.Jornada; // Para o mapa de jornadas
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFalta; // Para o método getDiaTrabalho
import com.fromzero.checkpoint.entities.SolicitacaoFerias;
import com.fromzero.checkpoint.entities.SolicitacaoFolga; // Para o método getDiaTrabalho

// Imports de Repositórios
import com.fromzero.checkpoint.repositories.FaltaRepository;
// import com.fromzero.checkpoint.repositories.FeriasRepository; // Se não for usar diretamente no resumo
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAbonoFaltaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFeriasRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;

// Imports de Serviços
import com.fromzero.checkpoint.services.JornadaService;

// Imports do Spring e Java Util
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek; // <<< IMPORTANTE
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Para getDiaTrabalho
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dias-trabalho")
@CrossOrigin(origins = "http://localhost:5173") // Ajuste se necessário
public class DiasTrabalhoController {

    private static final Logger log = LoggerFactory.getLogger(DiasTrabalhoController.class);

    @Autowired
    private JornadaService jornadaService;
    @Autowired
    private MarcacaoRepository marcacaoRepository;
    @Autowired
    private FaltaRepository faltaRepository;
    // @Autowired
    // private FeriasRepository feriasRepository; // Se for usar para o resumo, senão pode remover
    @Autowired
    private FolgaRepository folgaRepository;
    @Autowired
    private SolicitacaoFeriasRepository solicitacaoFeriasRepository;
    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository; // Para getDiaTrabalho
    @Autowired
    private SolicitacaoAbonoFaltaRepository solicitacaoAbonoFaltaRepository;


    @GetMapping("/{colaboradorId}")
    public ResponseEntity<Map<String, String>> getDiasTrabalho(
            @PathVariable Long colaboradorId,
            @RequestParam(required = false) Integer ano) {

        int anoParaProcessar = (ano != null) ? ano : LocalDate.now().getYear();
        log.info("CONTROLLER: Solicitado calendário OTIMIZADO para Colaborador ID: {} no Ano: {}", colaboradorId, anoParaProcessar);

        Map<String, String> diasComStatus = new TreeMap<>();
        LocalDate inicioDoAno = LocalDate.of(anoParaProcessar, 1, 1);
        LocalDate fimDoAno = LocalDate.of(anoParaProcessar, 12, 31);
        LocalDateTime inicioDoAnoTime = inicioDoAno.atStartOfDay();
        LocalDateTime fimDoAnoTimeMax = fimDoAno.atTime(LocalTime.MAX);
        LocalDateTime inicioDoProximoAnoTime = inicioDoAno.plusYears(1).atStartOfDay();

        // 1. BUSCAR TODOS OS EVENTOS RELEVANTES PARA O ANO
        List<Marcacao> todasMarcacoesDoAno = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
            colaboradorId, inicioDoAnoTime, fimDoAnoTimeMax
        );
        List<Falta> todasFaltasDoAno = faltaRepository.findByColaboradorIdAndCriadoEmBetweenYearRange(
            colaboradorId, inicioDoAnoTime, inicioDoProximoAnoTime
        );
        List<Folga> todasFolgasDoAno = folgaRepository.findByColaboradorIdAndDataBetween(
            colaboradorId, inicioDoAno, fimDoAno
        );
        List<SolicitacaoFerias> todasFeriasAprovadasDoAno = solicitacaoFeriasRepository
            .findAprovadasByColaboradorIdOverlappingYear(colaboradorId, inicioDoAno, fimDoAno);

        Map<LocalDate, Boolean> mapaDiasComMarcacao = todasMarcacoesDoAno.stream()
            .map(m -> m.getDataHora().toLocalDate())
            .distinct()
            .collect(Collectors.toMap(date -> date, date -> true));

        Map<LocalDate, Falta> mapaFaltas = todasFaltasDoAno.stream()
            .collect(Collectors.toMap(
                f -> f.getCriadoEm().toLocalDate(), // Assumindo que Falta tem getCriadoEm()
                falta -> falta,
                (f1, f2) -> f1 
            ));

        Map<LocalDate, Folga> mapaFolgas = todasFolgasDoAno.stream()
            .collect(Collectors.toMap(
                Folga::getData,
                folga -> folga,
                (f1, f2) -> f1
            ));
        
        Map<LocalDate, SolicitacaoFerias> mapaFerias = new HashMap<>();
        for (SolicitacaoFerias ferias : todasFeriasAprovadasDoAno) {
            // Assumindo que SolicitacaoFerias tem getDataInicio() e getDataFim()
            for (LocalDate d = ferias.getDataInicio(); !d.isAfter(ferias.getDataFim()); d = d.plusDays(1)) {
                if (d.getYear() == anoParaProcessar) {
                    mapaFerias.put(d, ferias);
                }
            }
        }

        Map<LocalDate, Jornada> mapaJornadasDoAno = jornadaService.getActiveJornadasMapForYear(colaboradorId, anoParaProcessar);
        log.info("TAMANHO MAPA JORNADAS DO ANO: " + mapaJornadasDoAno.size());

        for (LocalDate dataAtual = inicioDoAno; !dataAtual.isAfter(fimDoAno); dataAtual = dataAtual.plusDays(1)) {
            String statusFinalDoDia = null;
            log.info("Processando data: " + dataAtual);

            if (mapaFerias.containsKey(dataAtual)) {
                statusFinalDoDia = "ferias";
            } else if (mapaFolgas.containsKey(dataAtual)) {
                statusFinalDoDia = "folga";
            } else {
                Jornada jornadaDoDia = mapaJornadasDoAno.get(dataAtual);
                if (jornadaDoDia != null) {
                    String escala = jornadaDoDia.getEscala(); // Assumindo getEscala()
                    DayOfWeek diaDaSemana = dataAtual.getDayOfWeek();
                    log.info("Data: {}, Escala: {}, DiaSemana: {}", dataAtual, escala, diaDaSemana);
                    
                    boolean isDescanso = false;
                    if (escala != null) {
                        if ("6x1".equalsIgnoreCase(escala) && diaDaSemana == DayOfWeek.SUNDAY) isDescanso = true;
                        else if ("5x2".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) isDescanso = true;
                        else if ("4x3".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.FRIDAY || diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) isDescanso = true;
                    }
                    if (isDescanso) {
                        statusFinalDoDia = "DESCANSO_ESCALA";
                    }
                }
                if (statusFinalDoDia == null) { 
                    if (mapaFaltas.containsKey(dataAtual)) {
                        statusFinalDoDia = "falta";
                    } else if (mapaDiasComMarcacao.getOrDefault(dataAtual, false)) {
                        statusFinalDoDia = "normal";
                    }
                }
            } 
            
            if (statusFinalDoDia != null) {
                diasComStatus.put(dataAtual.toString(), statusFinalDoDia);
            }
        }
        return ResponseEntity.ok(diasComStatus);
    }

    // Método calcularHorasTrabalhadas (ajustado para tratar melhor os pares)
    private double calcularHorasTrabalhadas(List<Marcacao> marcacoesDoDia) {
        if (marcacoesDoDia == null || marcacoesDoDia.size() < 2) return 0;
    
        List<Marcacao> sortedMarcacoes = marcacoesDoDia.stream()
                .sorted(Comparator.comparing(Marcacao::getDataHora))
                .collect(Collectors.toList());
    
        double totalSegundosTrabalhados = 0;
        LocalDateTime entrada = null;
        LocalDateTime inicioPausa = null;
    
        for (Marcacao marcacao : sortedMarcacoes) {
            switch (marcacao.getTipo()) {
                case ENTRADA:
                    if (entrada == null) { // Considera a primeira entrada
                        entrada = marcacao.getDataHora();
                    }
                    break;
                case SAIDA:
                    if (entrada != null) { // Se tem uma entrada correspondente
                        totalSegundosTrabalhados += java.time.Duration.between(entrada, marcacao.getDataHora()).getSeconds();
                        entrada = null; // Reseta para o próximo par Entrada/Saída
                    }
                    break;
                case PAUSA:
                    if (entrada != null && inicioPausa == null) { // Inicia pausa se estiver trabalhando
                        inicioPausa = marcacao.getDataHora();
                    }
                    break;
                case RETOMADA:
                    if (entrada != null && inicioPausa != null) { // Se estava em pausa
                        totalSegundosTrabalhados -= java.time.Duration.between(inicioPausa, marcacao.getDataHora()).getSeconds(); // Subtrai tempo da pausa
                        inicioPausa = null; // Finaliza a pausa
                    }
                    break;
            }
        }
        // Se ficou uma entrada sem saída no final do dia (improvável se o sistema forçar saída), pode ser ignorado ou tratado.
        return totalSegundosTrabalhados / 3600.0; // Converte segundos para horas
    }
    

    // GET para resumo anual (ajustado para usar os métodos de repositório anuais)
    @GetMapping("/{colaboradorId}/resumo")
    public ResponseEntity<Map<String, Object>> getResumoTrabalho(
            @PathVariable Long colaboradorId,
            @RequestParam(required = false) Integer ano) {
        
        int anoParaProcessar = (ano != null) ? ano : LocalDate.now().getYear();
        LocalDate inicioDoAno = LocalDate.of(anoParaProcessar, 1, 1);
        LocalDate fimDoAno = LocalDate.of(anoParaProcessar, 12, 31);
        LocalDateTime inicioDoAnoTime = inicioDoAno.atStartOfDay();
        LocalDateTime fimDoAnoTimeMax = fimDoAno.atTime(LocalTime.MAX); // Ajuste para o último momento do dia
        LocalDateTime inicioDoProximoAnoTime = inicioDoAno.plusYears(1).atStartOfDay();
    
        Map<String, Object> resumo = new TreeMap<>();
    
        // Use as listas já buscadas se possível, ou métodos count específicos do repositório se eles existirem
        // para evitar buscar a lista inteira só para contar.
        // Se não tiver count específico, buscar a lista e pegar o .size() é uma opção.
        
        // Total de Faltas no ano
        List<Falta> faltasDoAno = faltaRepository.findByColaboradorIdAndCriadoEmBetweenYearRange(colaboradorId, inicioDoAnoTime, inicioDoProximoAnoTime);
        resumo.put("totalFaltas", (long) faltasDoAno.size());
    
        // Total de Folgas no ano
        List<Folga> folgasDoAno = folgaRepository.findByColaboradorIdAndDataBetween(colaboradorId, inicioDoAno, fimDoAno);
        resumo.put("totalFolgas", (long) folgasDoAno.size());
    
        // Total de dias de Férias APROVADAS no ano
        List<SolicitacaoFerias> feriasAprovadasDoAno = solicitacaoFeriasRepository.findAprovadasByColaboradorIdOverlappingYear(colaboradorId, inicioDoAno, fimDoAno);
        long totalDiasFerias = feriasAprovadasDoAno.stream()
            .mapToLong(sf -> {
                LocalDate inicioFeriasNoAno = sf.getDataInicio().isBefore(inicioDoAno) ? inicioDoAno : sf.getDataInicio();
                LocalDate fimFeriasNoAno = sf.getDataFim().isAfter(fimDoAno) ? fimDoAno : sf.getDataFim();
                // Garante que o período contado esteja dentro do ano
                if (fimFeriasNoAno.isBefore(inicioFeriasNoAno)) return 0L; // Período totalmente fora do ano após ajuste
                return ChronoUnit.DAYS.between(inicioFeriasNoAno, fimFeriasNoAno) + 1;
            })
            .sum();
        resumo.put("totalFerias", totalDiasFerias);
    
        // Total de Horas Trabalhadas no ano
        List<Marcacao> marcacoesDoAno = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDoAnoTime, fimDoAnoTimeMax);
        Map<LocalDate, List<Marcacao>> marcacoesPorDia = marcacoesDoAno.stream()
            .collect(Collectors.groupingBy(m -> m.getDataHora().toLocalDate()));
        double totalHorasTrabalhadas = marcacoesPorDia.values().stream()
            .mapToDouble(this::calcularHorasTrabalhadas)
            .sum();
        resumo.put("totalHorasTrabalhadas", String.format("%.2f", totalHorasTrabalhadas));
    
        return ResponseEntity.ok(resumo);
    }

    // GET para detalhes de um dia específico
    @GetMapping("/{colaboradorId}/{data}")
    public ResponseEntity<Map<String, Object>> getDiaTrabalho(
            @PathVariable Long colaboradorId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        Map<String, Object> response = new HashMap<>();
        String statusDoDia = "desconhecido";

        Optional<SolicitacaoFerias> feriasOpt = solicitacaoFeriasRepository.findAprovadaByColaboradorIdAndDateBetween(colaboradorId, data);
        if (feriasOpt.isPresent()) {
            SolicitacaoFerias s = feriasOpt.get();
            response.put("statusDia", "FERIAS");
            response.put("detalhes", Map.of(
                "id", s.getId(), "dataInicio", s.getDataInicio(), "dataFim", s.getDataFim(),
                "observacao", s.getObservacao() != null ? s.getObservacao() : "",
                "comentarioGestor", s.getComentarioGestor() != null ? s.getComentarioGestor() : "",
                "statusSolicitacao", s.getStatus()
            ));
            return ResponseEntity.ok(response);
        }

        Optional<SolicitacaoFolga> folgaOpt = solicitacaoFolgaRepository.findByColaboradorIdAndSolFolData(colaboradorId, data);
        if (folgaOpt.isPresent()) {
            SolicitacaoFolga s = folgaOpt.get();
            response.put("statusDia", "FOLGA");
            response.put("detalhes", Map.of(
                "id", s.getSolFolId(), "data", s.getSolFolData(), "saldoGasto", s.getSolFolSaldoGasto(),
                "observacao", s.getSolFolObservacao() != null ? s.getSolFolObservacao() : "",
                "statusSolicitacao", s.getSolFolStatus()
            ));
            return ResponseEntity.ok(response);
        }

        if (jornadaService.isDiaDeDescansoEscala(colaboradorId, data)) {
            response.put("statusDia", "DESCANSO_ESCALA");
            response.put("mensagem", "Dia de descanso pela escala.");
            return ResponseEntity.ok(response);
        }
        
        Optional<Falta> faltaOpt = faltaRepository.findByColaboradorIdAndCriadoEmOnDate(colaboradorId, data);
        if (faltaOpt.isPresent()) {
            Falta f = faltaOpt.get();
            response.put("statusDia", "FALTA");
            Map<String, Object> detalhesFalta = new HashMap<>();
            detalhesFalta.put("id", f.getId());
            detalhesFalta.put("tipoFalta", f.getTipo() != null ? f.getTipo().name() : "N/A");
            solicitacaoAbonoFaltaRepository.findByFaltaId(f.getId()).ifPresent(s -> {
                detalhesFalta.put("solicitacaoAbonoId", s.getId());
                detalhesFalta.put("motivoAbono", s.getMotivo());
                detalhesFalta.put("justificativaAbono", s.getJustificativa());
                detalhesFalta.put("statusAbono", s.getStatus() != null ? s.getStatus().name() : "N/A");
            });
            response.put("detalhes", detalhesFalta);
            return ResponseEntity.ok(response);
        }
        
        List<Marcacao> marcacoesDoDia = marcacaoRepository
            .findByColaboradorIdAndDataHoraBetween(colaboradorId, data.atStartOfDay(), data.atTime(LocalTime.MAX));
        if (!marcacoesDoDia.isEmpty()) {
            response.put("statusDia", "NORMAL");
            response.put("marcacoes", marcacoesDoDia.stream()
                .map(m -> Map.of("hora", m.getDataHora().toLocalTime().toString(), "tipo", m.getTipo().name()))
                .collect(Collectors.toList()));
            // Adicionar total de horas trabalhadas no dia
            double horasTrabalhadasNoDia = calcularHorasTrabalhadas(marcacoesDoDia);
            response.put("totalHorasDia", String.format("%.2f", horasTrabalhadasNoDia));
            return ResponseEntity.ok(response);
        }
        
        response.put("statusDia", statusDoDia.toUpperCase()); // Pode ser "DESCONHECIDO"
        return ResponseEntity.ok(response);
    }

    // Seu método getDiasPorTipo (ajustado para usar os métodos de repositório anuais)
    @GetMapping("/{colaboradorId}/filtro/{tipo}")
    public ResponseEntity<Map<String, String>> getDiasPorTipo(
            @PathVariable Long colaboradorId, 
            @PathVariable String tipo,
            @RequestParam(required = false) Integer ano) {
        
        int anoParaProcessar = (ano != null) ? ano : LocalDate.now().getYear();
        LocalDate inicioDoAno = LocalDate.of(anoParaProcessar, 1, 1);
        LocalDate fimDoAno = LocalDate.of(anoParaProcessar, 12, 31);
        LocalDateTime inicioDoAnoTime = inicioDoAno.atStartOfDay();
        LocalDateTime fimDoAnoTimeMax = fimDoAno.atTime(LocalTime.MAX);
        LocalDateTime inicioDoProximoAnoTime = inicioDoAno.plusYears(1).atStartOfDay();

        Map<LocalDate, String> diasFiltradosMap = new TreeMap<>(Comparator.reverseOrder());
    
        switch (tipo.toLowerCase()) {
            case "folga":
                folgaRepository.findByColaboradorIdAndDataBetween(colaboradorId, inicioDoAno, fimDoAno)
                    .forEach(folga -> diasFiltradosMap.put(folga.getData(), "folga"));
                break;
            case "falta":
                faltaRepository.findByColaboradorIdAndCriadoEmBetweenYearRange(colaboradorId, inicioDoAnoTime, inicioDoProximoAnoTime)
                    .forEach(falta -> diasFiltradosMap.put(falta.getCriadoEm().toLocalDate(), "falta"));
                break;
            case "ferias":
                 solicitacaoFeriasRepository.findAprovadasByColaboradorIdOverlappingYear(colaboradorId, inicioDoAno, fimDoAno)
                    .forEach(ferias -> {
                        for (LocalDate d = ferias.getDataInicio(); !d.isAfter(ferias.getDataFim()); d = d.plusDays(1)) {
                            if (d.getYear() == anoParaProcessar) diasFiltradosMap.put(d, "ferias");
                        }
                    });
                break;
            case "normal": // Alterado de "marcacao" para "normal" para consistência
                 marcacaoRepository.findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDoAnoTime, fimDoAnoTimeMax)
                    .stream()
                    .map(m -> m.getDataHora().toLocalDate())
                    .distinct()
                    .forEach(data -> diasFiltradosMap.put(data, "normal"));
                break;
            case "descanso_escala":
                Map<LocalDate, Jornada> mapaJornadas = jornadaService.getActiveJornadasMapForYear(colaboradorId, anoParaProcessar);
                for (LocalDate dataAtual = inicioDoAno; !dataAtual.isAfter(fimDoAno); dataAtual = dataAtual.plusDays(1)) {
                    Jornada jornadaDoDia = mapaJornadas.get(dataAtual);
                    if (jornadaDoDia != null) {
                        String escala = jornadaDoDia.getEscala();
                        DayOfWeek diaDaSemana = dataAtual.getDayOfWeek();
                        boolean isDescanso = false;
                        if (escala != null) {
                            if ("6x1".equalsIgnoreCase(escala) && diaDaSemana == DayOfWeek.SUNDAY) isDescanso = true;
                            else if ("5x2".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) isDescanso = true;
                            else if ("4x3".equalsIgnoreCase(escala) && (diaDaSemana == DayOfWeek.FRIDAY || diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY)) isDescanso = true;
                        }
                        if (isDescanso) {
                             // Para filtro, só adiciona se não for férias ou folga (que têm prioridade)
                            boolean isFerias = solicitacaoFeriasRepository.findAprovadaByColaboradorIdAndDateBetween(colaboradorId, dataAtual).isPresent();
                            boolean isFolga = solicitacaoFolgaRepository.findByColaboradorIdAndSolFolData(colaboradorId, dataAtual).isPresent();
                            if (!isFerias && !isFolga) {
                                diasFiltradosMap.put(dataAtual, "DESCANSO_ESCALA");
                            }
                        }
                    }
                }
                break;
            default:
                return ResponseEntity.badRequest().body(new HashMap<>()); // Retorna mapa vazio em vez de null
        }
        
        Map<String, String> responseMap = diasFiltradosMap.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue,
                (v1, v2) -> v1, // Em caso de chaves duplicadas (não deve acontecer com LocalDate)
                TreeMap::new // Mantém ordenação
            ));
        return ResponseEntity.ok(responseMap);
    }
}