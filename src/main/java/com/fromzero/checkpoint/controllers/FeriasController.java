package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.services.FeriasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ferias")
@CrossOrigin(origins = "http://localhost:5173")
public class FeriasController {

    @Autowired
    private FeriasService feriasService;

    @GetMapping("/saldo")
    public ResponseEntity<Double> getSaldoFerias(@RequestParam Integer colaboradorId) {
        try {
            Double saldo = feriasService.obterSaldoFerias(colaboradorId);
            return ResponseEntity.ok(saldo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}