package com.fromzero.checkpoint.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.dto.SolicitacaoGenericaDTO;
import com.fromzero.checkpoint.dto.SolicitacaoResumoDTO;
import com.fromzero.checkpoint.entities.Gestor;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFalta;
import com.fromzero.checkpoint.entities.SolicitacaoAjustePonto;
import com.fromzero.checkpoint.repositories.GestorRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAbonoFaltaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAjustePontoRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFeriasRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;

@RestController
public class GestorController {

    @Autowired
    private GestorRepository repository;

    @Autowired
    private SolicitacaoFolgaRepository folgaRepository;

    @Autowired
    private SolicitacaoFeriasRepository feriasRepository;

    @Autowired
    private SolicitacaoAbonoFaltaRepository abonoRepository;

    @Autowired
    private SolicitacaoAjustePontoRepository ajusteRepository;

    @PostMapping("/gestor")
    public Gestor cadastrarGestor(@RequestBody Gestor gestor) {
        return repository.save(gestor);
    }

    @GetMapping("/gestor")
    public List<Gestor> obterGestores() {
        return repository.findAll();
    }

    @GetMapping("/gestor/{id}")
    public ResponseEntity<Gestor> obterGestor(@PathVariable Long id) {
        return repository.findById(id)
            .map(gestor -> ResponseEntity.ok(gestor))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> logarColaborador(@RequestBody Gestor g) {
        Gestor gestor = repository.findByEmail(g.getEmail()).orElseThrow(
            () -> new RuntimeException("Gestor não encontrado")
        );

         if (gestor.getSenha().equals(g.getSenha())) {
             return ResponseEntity.ok(gestor);
         } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
         }
    }

    @GetMapping("/resumo-solicitacoes")
    public ResponseEntity<SolicitacaoResumoDTO> obterResumoSolicitacoes() {

        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);

        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);

        LocalDateTime inicioOntem = ontem.atStartOfDay();
        LocalDateTime fimOntem = ontem.atTime(LocalTime.MAX);

        long pendentes = folgaRepository.countBySolFolStatus("pendente")
                + feriasRepository.countByStatus("pendente")
                + abonoRepository.countByStatus(SolicitacaoAbonoFalta.SolicitacaoStatus.Pendente)
                + ajusteRepository.countByStatus(SolicitacaoAjustePonto.StatusMarcacao.pendente);

        long criadasHoje = folgaRepository.countByCriadoEmBetween(inicioHoje, fimHoje)
                + feriasRepository.countByCriadoEmBetween(inicioHoje, fimHoje)
                + abonoRepository.countByCriadoEmBetween(inicioHoje, fimHoje)
                + ajusteRepository.countByCriadoEmBetween(inicioHoje, fimHoje);

        long criadasOntem = folgaRepository.countByCriadoEmBetween(inicioOntem, fimOntem)
                + feriasRepository.countByCriadoEmBetween(inicioOntem, fimOntem)
                + abonoRepository.countByCriadoEmBetween(inicioOntem, fimOntem)
                + ajusteRepository.countByCriadoEmBetween(inicioOntem, fimOntem);

        long diferenca = criadasHoje - criadasOntem;

        SolicitacaoResumoDTO dto = new SolicitacaoResumoDTO(
                pendentes,
                criadasHoje,
                criadasOntem,
                diferenca
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ultimas-solicitacoes-pendentes")
    public ResponseEntity<List<SolicitacaoGenericaDTO>> obterUltimasSolicitacoesPendentes() {
        List<SolicitacaoGenericaDTO> todas = new ArrayList<>();

        folgaRepository.findTop4BySolFolStatusOrderByCriadoEmDesc("pendente").forEach(f -> {
            todas.add(new SolicitacaoGenericaDTO(
                    "FOLGA",
                    f.getSolFolId().longValue(),
                    f.getSolFolStatus(),
                    f.getCriadoEm(),
                    f.getColaborador() != null ? f.getColaborador().getNome() : null
            ));
        });

        feriasRepository.findTop4ByStatusOrderByCriadoEmDesc("pendente").forEach(f -> {
            todas.add(new SolicitacaoGenericaDTO(
                    "FERIAS",
                    f.getId(),
                    f.getStatus(),
                    f.getCriadoEm(),
                    f.getColaborador() != null ? f.getColaborador().getNome() : null
            ));
        });

        abonoRepository.findTop4ByStatusOrderByCriadoEmDesc(SolicitacaoAbonoFalta.SolicitacaoStatus.Pendente).forEach(f -> {
            todas.add(new SolicitacaoGenericaDTO(
                    "ABONO",
                    f.getId(),
                    f.getStatus().name(),
                    f.getCriadoEm(),
                    f.getFalta() != null && f.getFalta().getColaborador() != null ? 
                        f.getFalta().getColaborador().getNome() : null
            ));
        });

        ajusteRepository.findTop4ByStatusOrderByCriadoEmDesc(SolicitacaoAjustePonto.StatusMarcacao.pendente).forEach(f -> {
            todas.add(new SolicitacaoGenericaDTO(
                    "AJUSTE",
                    f.getId(),
                    f.getStatus().name(),
                    f.getCriadoEm(),
                    f.getColaboradorNome()  // já tem direto
            ));
        });

        List<SolicitacaoGenericaDTO> ultimasQuatro = todas.stream()
                .sorted(Comparator.comparing(
                        SolicitacaoGenericaDTO::getCriadoEm,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .limit(4)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ultimasQuatro);
    }
}
