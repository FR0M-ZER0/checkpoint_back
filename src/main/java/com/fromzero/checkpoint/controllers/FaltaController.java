package com.fromzero.checkpoint.controllers;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.dto.FaltaComColaboradorDTO;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;
import com.fromzero.checkpoint.services.RelatorioFaltasService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
public class FaltaController {
    @Autowired
    private FaltaRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RelatorioFaltasService relatorioFaltasService;
    
    @PostMapping("/falta")
    public Falta cadastrarFalta(@RequestBody Falta f) {
        Colaborador colaborador = colaboradorRepository.findById(f.getColaborador().getId())
            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        f.setColaborador(colaborador);
        repository.save(f);

        notificacaoService.criaNotificacao(
            "Suas férias foram aprovadas",
            NotificacaoTipo.ferias,
            colaborador
        );

        messagingTemplate.convertAndSend("/topic/solicitacoes", f);
        return f;
    }
    
    @GetMapping("/falta")
    public List<Falta> obterFaltas() {
        return repository.findAll();
    }
    
    @GetMapping("/falta/{id}")
    public Falta obterFalta(@PathVariable Long id) {
        return repository.findById(id).get();
    }

    @PutMapping("/falta/{id}")
    public ResponseEntity<Falta> atualizarFalta(@PathVariable Long id, @RequestBody Falta novaFalta) {
        return repository.findById(id)
            .map(faltaExistente -> {
                if (novaFalta.getTipo() != null) {
                    faltaExistente.setTipo(novaFalta.getTipo());
                }
                if (novaFalta.getJustificado() != null) {
                    faltaExistente.setJustificado(novaFalta.getJustificado());
                }
    
                return ResponseEntity.ok(repository.save(faltaExistente));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/falta/{id}")
    public ResponseEntity<?> deletarFalta(@PathVariable Long id) {
        return repository.findById(id)
            .map(falta -> {
                repository.delete(falta);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/falta/diferenca-dia")
    public ResponseEntity<Integer> diferencaFaltasEntreHojeEOntem() {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimHoje = inicioHoje.plusDays(1);

        LocalDateTime inicioOntem = inicioHoje.minusDays(1);
        LocalDateTime fimOntem = inicioHoje;

        int faltasHoje = repository.countByCriadoEmBetween(inicioHoje, fimHoje);
        int faltasOntem = repository.countByCriadoEmBetween(inicioOntem, fimOntem);

        int diferenca = faltasHoje - faltasOntem;

        return ResponseEntity.ok(diferenca);
    }

    @GetMapping("/falta/filtro")
    public ResponseEntity<List<FaltaComColaboradorDTO>> buscarFaltasPorFiltro(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @RequestParam(required = false) Boolean justificado,
        @RequestParam(required = false) Falta.TipoFalta tipo
    ) {
        LocalDate dataFiltro = (data != null) ? data : LocalDate.now();
        LocalDateTime inicio = dataFiltro.atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);

        List<Falta> faltas = repository.findByFilters(inicio, fim, justificado, tipo);

        List<FaltaComColaboradorDTO> resultado = faltas.stream()
            .map(FaltaComColaboradorDTO::new)
            .toList();

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/relatorio-faltas")
    public ResponseEntity<InputStreamResource> gerarRelatorioFaltas(
            @RequestParam(required = false) Long colaboradorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) throws Exception {
        ByteArrayInputStream bis = relatorioFaltasService.gerarRelatorio(colaboradorId, dataInicio, dataFim);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio-faltas.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
