package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.CargaDiaria;
import com.fromzero.checkpoint.entities.Jornada;
import com.fromzero.checkpoint.repositories.CargaDiariaRepository;
import com.fromzero.checkpoint.repositories.JornadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cargas-diarias")
public class CargaDiariaController {

    @Autowired
    private CargaDiariaRepository cargaDiariaRepository;

    @Autowired
    private JornadaRepository jornadaRepository;

    @PostMapping
    public CargaDiaria criarCargaDiaria(@RequestBody CargaDiaria cargaDiaria) {
        // Verifica se a jornada existe
        Jornada jornada = jornadaRepository.findById(cargaDiaria.getJornada().getId())
                .orElseThrow(() -> new RuntimeException("Jornada não encontrada"));
        
        cargaDiaria.setJornada(jornada);
        return cargaDiariaRepository.save(cargaDiaria);
    }

    @GetMapping("/{id}")
    public CargaDiaria obterCargaDiariaPorId(@PathVariable Long id) {
        return cargaDiariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carga diária não encontrada"));
    }
}