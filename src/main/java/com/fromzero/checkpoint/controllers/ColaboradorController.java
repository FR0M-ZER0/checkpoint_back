package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.NotificacaoRepository;
import com.fromzero.checkpoint.services.NotificacaoService;



@RestController
public class ColaboradorController {
    @Autowired
    private ColaboradorRepository repository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @PostMapping("/colaborador")
    public Colaborador cadastrarColaborador(@RequestBody Colaborador c) {
        repository.save(c);
        return c;
    }

    @GetMapping("/colaborador")
    public List<Colaborador> obterColaboradores() {
        return repository.findAll();
    }

    @GetMapping("/colaborador/{id}")
    public Colaborador obterColaborador(@PathVariable Long id) {
        return repository.findById(id).get();
    }

    @GetMapping("/colaborador/faltas/{id}")
    public List<Falta> obterFaltasPorColaborador(@PathVariable Long id) {
        return faltaRepository.findByColaboradorId(id);
    }

    @GetMapping("/colaborador/falta/sem-solicitacao/{id}")
    public List<Falta> obterFaltasSemSolicitacao(@PathVariable Long id) {
        return faltaRepository.obterFaltasSemSolicitacao(id);
    }

    @GetMapping("/colaborador/notificacoes/{id}")
    public List<Notificacao> obterNotificacoesNaoLidas(@PathVariable Long id) {
        return notificacaoService.buscarNotificacoesNaoLidas(id);
    }

    @PutMapping("/colaborador/notificacao/{notificacaoId}")
    public Notificacao marcarNotificacaoComoLida(@PathVariable Long notificacaoId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);

        return notificacao;
    }

    @PostMapping("/login")
    public ResponseEntity<?> logarColaborador(@RequestBody Colaborador c) {
        Colaborador colaborador = repository.findByEmail(c.getEmail()).orElseThrow(
            () -> new RuntimeException("Colaborador não encontrado")
        );

         if (colaborador.getSenhaHash().equals(c.getSenhaHash())) {
             return ResponseEntity.ok(colaborador);
         } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
         }
    }
}
