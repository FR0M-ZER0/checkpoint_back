package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;

@RestController
public class ColaboradorController {
    @Autowired
    private ColaboradorRepository repository;

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
}
