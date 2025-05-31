package com.fromzero.checkpoint.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCodigoVerificacao(String para, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(para);
        mensagem.setSubject("Seu código de verificação");
        mensagem.setText("Seu código de verificação é: " + codigo +
                "\n\nEste código expira em 5 minutos." +
                "\n\nCaso você não tenha solicitado, ignore este e-mail.");
        mailSender.send(mensagem);
        System.out.println("📧 E-mail de verificação enviado para " + para);
    }
}