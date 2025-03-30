package com.fromzero.checkpoint.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fromzero.checkpoint.entities.Resposta;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Resposta.TipoResposta;
import com.fromzero.checkpoint.repositories.RespostaRepository;

@Service
public class RespostaService {

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Resposta criarResposta(String mensagem, TipoResposta tipo, Colaborador colaborador) {
        Resposta resposta = new Resposta();
        resposta.setMensagem(mensagem);
        resposta.setTipo(tipo);
        resposta.setColaborador(colaborador);
        resposta.setLida(false);

        resposta = respostaRepository.save(resposta);

        messagingTemplate.convertAndSend("/topic/respostas", resposta);

        return resposta;
    }

    public List<Resposta> buscarRespostasNaoLidas(Long colaboradorId) {
        return respostaRepository.findByColaboradorIdAndLidaFalse(colaboradorId);
    }
}
