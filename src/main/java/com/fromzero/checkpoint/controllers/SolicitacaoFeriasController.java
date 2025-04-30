package com.fromzero.checkpoint.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.entities.SolicitacaoFerias;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFeriasRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

@RestController
@RequestMapping("/solicitacao-ferias")
public class SolicitacaoFeriasController {
    
    @Autowired
    private SolicitacaoFeriasRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private FeriasRepository feriasRepository;

    @GetMapping
    public List<SolicitacaoFerias> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoFerias> getById(@PathVariable Long id) {
        Optional<SolicitacaoFerias> solicitacao = repository.findById(id);
        if (solicitacao.isPresent()) {
            return ResponseEntity.ok(solicitacao.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createSolicitacaoFerias(@RequestBody SolicitacaoFerias solicitacaoFerias) {
        try {
            Long colaboradorId = solicitacaoFerias.getColaboradorId();
            if (colaboradorId == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "ColaboradorId é obrigatório"));
            }

            long diasSolicitados = java.time.temporal.ChronoUnit.DAYS.between(
                    solicitacaoFerias.getDataInicio(), solicitacaoFerias.getDataFim()) + 1;

            if (diasSolicitados <= 0) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Período de férias inválido"));
            }

            List<Ferias> feriasList = feriasRepository.findByColaboradorId(colaboradorId);

            int diasUsados = feriasList.stream()
                    .mapToInt(f -> (int) java.time.temporal.ChronoUnit.DAYS.between(f.getDataInicio(), f.getDataFim()) + 1)
                    .sum();

            int saldoRestante = 30 - diasUsados;

            if (diasSolicitados > saldoRestante) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Saldo de férias insuficiente. Saldo atual: " + saldoRestante + " dias"));
            }

            SolicitacaoFerias novaSolicitacao = repository.save(solicitacaoFerias);

            Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

            notificacaoService.criaNotificacao(
                "Sua solicitação de férias foi enviada",
                NotificacaoTipo.ferias,
                colaborador
            );

            messagingTemplate.convertAndSend("/topic/solicitacoes", novaSolicitacao);

            return ResponseEntity.ok(novaSolicitacao);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao processar solicitação de férias"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitacaoFerias> updateSolicitacaoFerias(@PathVariable Long id, @RequestBody SolicitacaoFerias solicitacaoFeriasAtualizada) {
        Optional<SolicitacaoFerias> solicitacaoExistente = repository.findById(id);
        
        if (solicitacaoExistente.isPresent()) {
            SolicitacaoFerias solicitacao = solicitacaoExistente.get();
            
            if (solicitacaoFeriasAtualizada.getDataInicio() != null) {
                solicitacao.setDataInicio(solicitacaoFeriasAtualizada.getDataInicio());
            }
            if (solicitacaoFeriasAtualizada.getDataFim() != null) {
                solicitacao.setDataFim(solicitacaoFeriasAtualizada.getDataFim());
            }
            if (solicitacaoFeriasAtualizada.getObservacao() != null) {
                solicitacao.setObservacao(solicitacaoFeriasAtualizada.getObservacao());
            }
            if (solicitacaoFeriasAtualizada.getStatus() != null) {
                solicitacao.setStatus(solicitacaoFeriasAtualizada.getStatus());
            }
            if (solicitacaoFeriasAtualizada.getComentarioGestor() != null) {
                solicitacao.setComentarioGestor(solicitacaoFeriasAtualizada.getComentarioGestor());
            }
            if (solicitacaoFeriasAtualizada.getColaboradorId() != null) {
                solicitacao.setColaboradorId(solicitacaoFeriasAtualizada.getColaboradorId());
            }
    
            SolicitacaoFerias solicitacaoAtualizada = repository.save(solicitacao);
            return ResponseEntity.ok(solicitacaoAtualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolicitacaoFerias(@PathVariable Long id) {
        Optional<SolicitacaoFerias> solicitacao = repository.findById(id);
        
        if (solicitacao.isPresent()) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
