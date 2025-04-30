package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
public class FaltaController {
    @Autowired
    private FaltaRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/falta")
    public Falta cadastrarFalta(@RequestBody Falta f) {
        Colaborador colaborador = colaboradorRepository.findById(f.getColaborador().getId())
            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        f.setColaborador(colaborador);
        repository.save(f);

        notificacaoService.criaNotificacao(
            "Suas férias foram aprovadas",
            NotificacaoTipo.ferias,
            colaborador
        );

        messagingTemplate.convertAndSend("/topic/solicitacoes", f);
        return f;
    }
    
    @GetMapping("/falta")
    public List<Falta> obterFaltas() {
        return repository.findAll();
    }
    
    @GetMapping("/falta/{id}")
    public Falta obterFalta(@PathVariable Long id) {
        return repository.findById(id).get();
    }

    @PutMapping("/falta/{id}")
    public ResponseEntity<Falta> atualizarFalta(@PathVariable Long id, @RequestBody Falta novaFalta) {
        return repository.findById(id)
            .map(faltaExistente -> {
                if (novaFalta.getTipo() != null) {
                    faltaExistente.setTipo(novaFalta.getTipo());
                }
                if (novaFalta.getJustificado() != null) {
                    faltaExistente.setJustificado(novaFalta.getJustificado());
                }
    
                return ResponseEntity.ok(repository.save(faltaExistente));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/falta/{id}")
    public ResponseEntity<?> deletarFalta(@PathVariable Long id) {
        return repository.findById(id)
            .map(falta -> {
                repository.delete(falta);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
