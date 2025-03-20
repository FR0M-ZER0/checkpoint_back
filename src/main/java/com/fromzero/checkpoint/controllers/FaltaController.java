package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
public class FaltaController {
    @Autowired
    private FaltaRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;
    
    @PostMapping("/falta")
    public Falta cadastrarFalta(@RequestBody Falta f) {
        Colaborador colaborador = colaboradorRepository.findById(f.getColaborador().getId())
            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        f.setColaborador(colaborador);
        repository.save(f);  
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
}
