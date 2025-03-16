package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.models.Ferias;
import com.fromzero.checkpoint.services.FeriasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ferias")
public class FeriasController {

    private final FeriasService feriasService;

    public FeriasController(FeriasService feriasService) {
        this.feriasService = feriasService;
    }

    @PostMapping
    public ResponseEntity<Ferias> solicitarFerias(@RequestBody Ferias ferias) {
        Ferias novaFerias = feriasService.solicitarFerias(ferias);
        return ResponseEntity.ok(novaFerias);
    }

    @GetMapping("/{colaboradorId}")
    public ResponseEntity<List<Ferias>> listarFerias(@PathVariable Long colaboradorId) {
        return ResponseEntity.ok(feriasService.listarFerias(colaboradorId));
    }
}