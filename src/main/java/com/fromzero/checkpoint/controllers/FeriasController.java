package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.services.FeriasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ferias")
@RequiredArgsConstructor
public class FeriasController {

    private final FeriasService feriasService;

    @PostMapping
    public ResponseEntity<?> solicitarFerias(@RequestBody Ferias ferias) {
        if (!ferias.isPeriodoValido()) {
            return ResponseEntity.badRequest().body("Período mínimo de férias é de 14 dias");
        }

        if (ferias.getDiasUteis() < 14) {
            return ResponseEntity.badRequest().body("O período deve conter pelo menos 14 dias úteis");
        }

        Ferias feriasSalvas = feriasService.solicitarFerias(ferias);
        return ResponseEntity.ok(feriasSalvas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ferias> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(feriasService.buscarPorId(id));
    }

    @GetMapping("/colaborador/{colaboradorId}")
    public ResponseEntity<List<Ferias>> listarPorColaborador(@PathVariable Long colaboradorId) {
        return ResponseEntity.ok(feriasService.listarPorColaborador(colaboradorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ferias> atualizar(@PathVariable Long id, @RequestBody Ferias ferias) {
        return ResponseEntity.ok(feriasService.atualizar(id, ferias));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        feriasService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}