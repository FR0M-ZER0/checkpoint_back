package com.fromzero.checkpoint.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fromzero.checkpoint.services.FolgaService;
import com.fromzero.checkpoint.entities.Folga;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;
import java.util.List;
import java.util.Map;  // <<< ADICIONE ESTE IMPORT

@RestController
@RequestMapping("/api/folga")
@CrossOrigin(origins = "http://localhost:5173") // ou a porta do seu frontend
public class FolgaController {

    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository;

    @Autowired
    private FolgaService folgaService;

    @Autowired
    private FolgaRepository repository;

    @GetMapping("/saldo")
    public ResponseEntity<String> getSaldoHoras(@RequestParam Long colaboradorId) {
        try {
            String saldo = folgaService.obterSaldoHoras(colaboradorId);
            return ResponseEntity.ok(saldo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Retorna a mensagem de erro
        }
    }
    @GetMapping // Responde a GET /api/folga?colaboradorId=X
    public ResponseEntity<?> getSolicitacoesDeFolgaPorColaborador(@RequestParam Long colaboradorId) {
        try {
            // Assumindo que SolicitacaoFolgaRepository tem findByColaboradorId
            List<SolicitacaoFolga> solicitacoes = solicitacaoFolgaRepository.findByColaboradorId(colaboradorId);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            // Adicione log aqui se tiver
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro ao buscar histórico de folgas."));
        }
    }


    @PostMapping
    public Folga createFolga(@RequestBody Folga f) {
        return repository.save(f);
    }
}