package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoEmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarNotificacaoParaGestor(String emailGestor, String nomeColaborador, String saldoHora) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailGestor);
        message.setSubject("Novo registro de hora extra");
        message.setText("O colaborador " + nomeColaborador + " registrou " + saldoHora + " de hora extra.");
        mailSender.send(message);
    }
}

