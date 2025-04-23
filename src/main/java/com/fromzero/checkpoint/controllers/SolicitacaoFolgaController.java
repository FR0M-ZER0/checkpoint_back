package com.fromzero.checkpoint.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

@RestController
@RequestMapping("/solicitacao-folga")
public class SolicitacaoFolgaController {
    @Autowired
    private SolicitacaoFolgaRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public SolicitacaoFolga create(@RequestBody SolicitacaoFolga s) {
    	s.setSolFolTipo("Folga");
        repository.save(s);

        Colaborador colaborador = colaboradorRepository.findById(s.getColaboradorId()).orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        notificacaoService.criaNotificacao("Sua solicitação de ajuste na marcação do ponto foi enviada", NotificacaoTipo.ponto, colaborador);

        messagingTemplate.convertAndSend("/topic/solicitacoes", s);

        return s;
    }

    @GetMapping
    public List<SolicitacaoFolga> getAll() {
        return repository.findAll();
    }

    @GetMapping("/pendentes")
    public List<SolicitacaoFolga> getPendentes() {
        return repository.findBySolFolStatus(SolicitacaoFolga.Status.Pendente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitacaoFolga> responderSolicitacao(@PathVariable Long id, @RequestBody SolicitacaoFolga updatedSolicitacao) {
        Optional<SolicitacaoFolga> optionalSolicitacao = repository.findById(id);

        if (optionalSolicitacao.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SolicitacaoFolga existingSolicitacao = optionalSolicitacao.get();
        existingSolicitacao.setSolFolStatus(updatedSolicitacao.getSolFolStatus());

        SolicitacaoFolga savedSolicitacao = repository.save(existingSolicitacao);
        return new ResponseEntity<>(savedSolicitacao, HttpStatus.OK);
    }
}
