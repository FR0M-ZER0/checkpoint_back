package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.CargaDiaria;
import com.fromzero.checkpoint.entities.CargaFlexivel;
import com.fromzero.checkpoint.entities.Jornada;
import com.fromzero.checkpoint.repositories.JornadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jornadas")
public class JornadaController {

    @Autowired
    private JornadaRepository jornadaRepository;

    @PostMapping
    public Jornada criarJornada(@RequestBody Jornada jornada) {
        return jornadaRepository.save(jornada);
    }

    @GetMapping
    public List<Jornada> listarTodasJornadas() {
        return jornadaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Jornada obterJornadaPorId(@PathVariable Long id) {
        return jornadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jornada não encontrada com id: " + id));
    }

    @GetMapping("/{id}/cargas-diarias")
    public List<CargaDiaria> listarCargasDiariasDaJornada(@PathVariable Long id) {
        Jornada jornada = jornadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jornada não encontrada"));
        return jornada.getCargasDiarias();
    }

    @GetMapping("/{id}/cargas-flexiveis")
    public List<CargaFlexivel> listarCargasFlexiveisDaJornada(@PathVariable Long id) {
        Jornada jornada = jornadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jornada não encontrada"));
        return jornada.getCargasFlexiveis();
    }
}