package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Gestor;
import com.fromzero.checkpoint.repositories.GestorRepository;

@RestController
public class GestorController {

    @Autowired
    private GestorRepository repository;

    @PostMapping("/gestor")
    public Gestor cadastrarGestor(@RequestBody Gestor gestor) {
        return repository.save(gestor);
    }

    @GetMapping("/gestor")
    public List<Gestor> obterGestores() {
        return repository.findAll();
    }

    @GetMapping("/gestor/{id}")
    public ResponseEntity<Gestor> obterGestor(@PathVariable Long id) {
        return repository.findById(id)
            .map(gestor -> ResponseEntity.ok(gestor))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> logarColaborador(@RequestBody Gestor g) {
        Gestor gestor = repository.findByEmail(g.getEmail()).orElseThrow(
            () -> new RuntimeException("Gestor não encontrado")
        );

         if (gestor.getSenha().equals(g.getSenha())) {
             return ResponseEntity.ok(gestor);
         } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
         }
    }
}
