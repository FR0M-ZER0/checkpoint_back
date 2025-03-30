package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.CargaFlexivel;
import com.fromzero.checkpoint.entities.Jornada;
import com.fromzero.checkpoint.repositories.CargaFlexivelRepository;
import com.fromzero.checkpoint.repositories.JornadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cargas-flexiveis")
public class CargaFlexivelController {

    @Autowired
    private CargaFlexivelRepository cargaFlexivelRepository;

    @Autowired
    private JornadaRepository jornadaRepository;

    @PostMapping
    public CargaFlexivel criarCargaFlexivel(@RequestBody CargaFlexivel cargaFlexivel) {
        // Verifica se a jornada existe
        Jornada jornada = jornadaRepository.findById(cargaFlexivel.getJornada().getId())
                .orElseThrow(() -> new RuntimeException("Jornada não encontrada"));
        
        cargaFlexivel.setJornada(jornada);
        return cargaFlexivelRepository.save(cargaFlexivel);
    }

    @GetMapping("/{id}")
    public CargaFlexivel obterCargaFlexivelPorId(@PathVariable Long id) {
        return cargaFlexivelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carga flexível não encontrada"));
    }
}