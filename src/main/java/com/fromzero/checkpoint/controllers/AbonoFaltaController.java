package com.fromzero.checkpoint.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFalta;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAbonoFaltaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AbonoFaltaController {
    @Autowired
    private SolicitacaoAbonoFaltaRepository repository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @PostMapping("/abonar-falta")
    public SolicitacaoAbonoFalta cadastrarAbonoFalta(@RequestBody SolicitacaoAbonoFalta s) {
        Falta falta = faltaRepository.findById(s.getFalta().getId())
            .orElseThrow(() -> new RuntimeException("Falta não existe"));
        s.setFalta(falta);
        repository.save(s);

        notificacaoService.criaNotificacao("Sua solicitação de abono de falta foi solicitada", NotificacaoTipo.abono);
        return s;
    }
    
}
