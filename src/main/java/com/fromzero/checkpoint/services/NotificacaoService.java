package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.NotificacaoRepository;

@Service
public class NotificacaoService {
    @Autowired
    private NotificacaoRepository repository;

    public void criaNotificacao(String mensagem, NotificacaoTipo tipo) {
        Notificacao notificacao = new com.fromzero.checkpoint.entities.Notificacao();

        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        repository.save(notificacao);
    }
}
