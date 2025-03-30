package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fromzero.checkpoint.entities.ColaboradorJornada;
import com.fromzero.checkpoint.repositories.ColaboradorJornadaRepository;

@RestController
@RequestMapping("/colaboradores-jornadas")
public class ColaboradorJornadaController {
    
    @Autowired
    private ColaboradorJornadaRepository repository;
    
    @PostMapping
    public ColaboradorJornada vincularJornada(@RequestBody ColaboradorJornada colaboradorJornada) {
        return repository.save(colaboradorJornada);
    }
    
    @GetMapping
    public List<ColaboradorJornada> listarTodosVinculos() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public ColaboradorJornada obterVinculoPorId(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado com id: " + id));
    }
}