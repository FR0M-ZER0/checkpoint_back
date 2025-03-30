package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.SolicitacaoAjustePonto;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAjustePontoRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ajuste-ponto")
public class SolicitacaoAjustePontoController {
    @Autowired
    private SolicitacaoAjustePontoRepository repository;

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/solicitacao")
    public SolicitacaoAjustePonto create(@RequestBody SolicitacaoAjustePonto s) {
        repository.save(s);

        Marcacao marcacao = marcacaoRepository.findById(s.getMarcacaoId()).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        Colaborador colaborador = colaboradorRepository.findById(marcacao.getColaboradorId()).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));

        notificacaoService.criaNotificacao("Sua solicitação de ajuste na marcação do ponto foi enviada", NotificacaoTipo.ponto, colaborador);

        messagingTemplate.convertAndSend("/topic/solicitacoes", s);

        return s;
    }

    @GetMapping("/solicitacao")
    public List<SolicitacaoAjustePonto> getAll() {
        return repository.findAll();
    }

    @GetMapping("/solicitacao/pendentes")
    public List<SolicitacaoAjustePonto> getPendentes() {
        return repository.findByStatus(SolicitacaoAjustePonto.StatusMarcacao.pendente);
    }

    @PutMapping("/solicitacao/{id}")
    public ResponseEntity<SolicitacaoAjustePonto> responderSolicitacao(@PathVariable String id, @RequestBody SolicitacaoAjustePonto updatedSolicitacao) {
        Optional<SolicitacaoAjustePonto> optionalSolicitacao = repository.findById(id);

        if (optionalSolicitacao.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SolicitacaoAjustePonto existingSolicitacao = optionalSolicitacao.get();
        existingSolicitacao.setStatus(updatedSolicitacao.getStatus());

        SolicitacaoAjustePonto savedSolicitacao = repository.save(existingSolicitacao);
        return new ResponseEntity<>(savedSolicitacao, HttpStatus.OK);
    }
}
