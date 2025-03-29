package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.SolicitacaoAjustePonto;
import com.fromzero.checkpoint.repositories.SolicitacaoAjustePontoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ajuste-ponto")
public class SolicitacaoAjustePontoController {
    @Autowired
    private SolicitacaoAjustePontoRepository repository;

    @PostMapping("/solicitacao")
    public SolicitacaoAjustePonto create(@RequestBody SolicitacaoAjustePonto s) {
        repository.save(s);
        return s;
    }

    @GetMapping("/solicitacao")
    public List<SolicitacaoAjustePonto> getAll() {
        return repository.findAll();
    }

    @PutMapping("/solicitacao/{id}")
    public ResponseEntity<SolicitacaoAjustePonto> update(@PathVariable String id, @RequestBody SolicitacaoAjustePonto updatedSolicitacao) {
        Optional<SolicitacaoAjustePonto> optionalSolicitacao = repository.findById(id);

        if (optionalSolicitacao.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SolicitacaoAjustePonto existingSolicitacao = optionalSolicitacao.get();

        existingSolicitacao.setMarcacaoId(updatedSolicitacao.getMarcacaoId());
        existingSolicitacao.setPeriodo(updatedSolicitacao.getPeriodo());
        existingSolicitacao.setTipo(updatedSolicitacao.getTipo());
        existingSolicitacao.setStatus(updatedSolicitacao.getStatus());
        existingSolicitacao.setObservacao(updatedSolicitacao.getObservacao());
        existingSolicitacao.setHorario(updatedSolicitacao.getHorario());

        SolicitacaoAjustePonto savedSolicitacao = repository.save(existingSolicitacao);
        return new ResponseEntity<>(savedSolicitacao, HttpStatus.OK);
    }
}
