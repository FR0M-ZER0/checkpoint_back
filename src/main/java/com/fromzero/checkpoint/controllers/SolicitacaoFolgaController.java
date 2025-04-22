package com.fromzero.checkpoint.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

@RestController
@RequestMapping("/solicitacao-folga")
public class SolicitacaoFolgaController {

    @Autowired
    private SolicitacaoFolgaRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @GetMapping
    public List<SolicitacaoFolga> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoFolga> getById(@PathVariable Integer id) {
        Optional<SolicitacaoFolga> solicitacao = repository.findById(id);
        if (solicitacao.isPresent()) {
            return ResponseEntity.ok(solicitacao.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<SolicitacaoFolga> createSolicitacaoFolga(@RequestBody SolicitacaoFolga solicitacaoFolga) {
        SolicitacaoFolga novaSolicitacao = repository.save(solicitacaoFolga);

        Colaborador colaborador = colaboradorRepository.findById(novaSolicitacao.getColaboradorId())
            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

        notificacaoService.criaNotificacao(
            "Sua solicitação de folga foi enviada",
            NotificacaoTipo.folga,
            colaborador
        );

        messagingTemplate.convertAndSend("/topic/solicitacoes", novaSolicitacao);

        return ResponseEntity.ok(novaSolicitacao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitacaoFolga> updateSolicitacaoFolga(@PathVariable Integer id, @RequestBody SolicitacaoFolga solicitacaoFolgaAtualizada) {
        Optional<SolicitacaoFolga> solicitacaoExistente = repository.findById(id);
        
        if (solicitacaoExistente.isPresent()) {
            SolicitacaoFolga solicitacao = solicitacaoExistente.get();
            
            if (solicitacaoFolgaAtualizada.getSolFolData() != null) {
                solicitacao.setSolFolData(solicitacaoFolgaAtualizada.getSolFolData());
            }
            if (solicitacaoFolgaAtualizada.getSolFolObservacao() != null) {
                solicitacao.setSolFolObservacao(solicitacaoFolgaAtualizada.getSolFolObservacao());
            }
            if (solicitacaoFolgaAtualizada.getSolFolStatus() != null) {
                solicitacao.setSolFolStatus(solicitacaoFolgaAtualizada.getSolFolStatus());
            }
            if (solicitacaoFolgaAtualizada.getColaboradorId() != null) {
                solicitacao.setColaboradorId(solicitacaoFolgaAtualizada.getColaboradorId());
            }
            if (solicitacaoFolgaAtualizada.getSolFolSaldoGasto() != null) {
                solicitacao.setSolFolSaldoGasto(solicitacaoFolgaAtualizada.getSolFolSaldoGasto());
            }

            SolicitacaoFolga solicitacaoAtualizada = repository.save(solicitacao);
            return ResponseEntity.ok(solicitacaoAtualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolicitacaoFolga(@PathVariable Integer id) {
        Optional<SolicitacaoFolga> solicitacao = repository.findById(id);
        
        if (solicitacao.isPresent()) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
