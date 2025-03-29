package com.fromzero.checkpoint.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fromzero.checkpoint.services.FeriasService;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFerias; // Verifique se este é o caminho correto
import com.fromzero.checkpoint.entities.Ferias; // Verifique se este é o caminho correto

@RestController
@RequestMapping("/api/ferias")
@CrossOrigin(origins = "http://localhost:5173")
public class FeriasController {

    @Autowired
    private FeriasService feriasService;

    @GetMapping("/saldo")
    public ResponseEntity<?> getSaldoFerias(@RequestParam Long colaboradorId) {
        try {
            if (colaboradorId == null) {
                return ResponseEntity.badRequest().body("ID do colaborador é obrigatório");
            }
            
            Double saldo = feriasService.obterSaldoFerias(colaboradorId);
            return ResponseEntity.ok(saldo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                            .body("Erro ao processar requisição");
        }
    }

    @PostMapping("/vender")
    public ResponseEntity<SolicitacaoAbonoFerias> venderFerias(@RequestBody SolicitacaoAbonoFerias abono) { // Corrigido o tipo
        try {
            SolicitacaoAbonoFerias abonoVendido = feriasService.venderFerias(abono);
            return ResponseEntity.ok(abonoVendido);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/agendar")
    public ResponseEntity<Ferias> agendarFerias(@RequestBody Ferias ferias) {
        try {
            Ferias feriasAgendadas = feriasService.agendarFerias(ferias);
            return ResponseEntity.ok(feriasAgendadas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ... outros endpoints ...
}