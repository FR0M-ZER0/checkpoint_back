package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.NotificacaoRepository;

@Service
public class NotificacaoService {
    @Autowired
    private NotificacaoRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notificacao criaNotificacao(String mensagem, NotificacaoTipo tipo) {
        Notificacao notificacao = new Notificacao();

        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        repository.save(notificacao);

        messagingTemplate.convertAndSend("/topic/notificacoes", notificacao);

        return notificacao;
    }
}
