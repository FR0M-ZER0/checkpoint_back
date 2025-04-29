package com.fromzero.checkpoint.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fromzero.checkpoint.services.FolgaService;
import com.fromzero.checkpoint.entities.Folga;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/folga")
@CrossOrigin(origins = "http://localhost:5173") // ou a porta do seu frontend
public class FolgaController {

    private static final Logger log = LoggerFactory.getLogger(FolgaController.class);

    @Autowired
    private FolgaService folgaService;

    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository;

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

    @PostMapping 
    public ResponseEntity<?> createFolga(@RequestBody SolicitacaoFolga solicitacao) { // <<< Recebe SolicitacaoFolga
        try {
            // Validações básicas (data é validada pelo @JsonFormat ou aqui)
             if (solicitacao.getSolFolSaldoGasto() == null || solicitacao.getSolFolSaldoGasto().isBlank()) {
                 return ResponseEntity.badRequest().body(Map.of("erro", "O campo 'saldoGasto' é obrigatório."));
            }
             if (solicitacao.getColaboradorId() == null) {
                 return ResponseEntity.badRequest().body(Map.of("erro", "O campo 'colaboradorId' é obrigatório."));
             }
             // Define status padrão PENDENTE se não vier
             if (solicitacao.getSolFolStatus() == null || solicitacao.getSolFolStatus().isBlank()) {
                 solicitacao.setSolFolStatus("PENDENTE");
             }
            // TODO: Chamar service para validar saldo de horas antes de salvar?
    
            // Salva usando o repositório CORRETO
            SolicitacaoFolga folgaSalva = solicitacaoFolgaRepository.save(solicitacao); 
            return ResponseEntity.status(HttpStatus.CREATED).body(folgaSalva); // Retorna 201 Created
    
        } catch (Exception e) { // Captura genérica, idealmente trate exceções específicas
             log.error("Erro ao criar solicitação de folga", e); // Use o Logger se o declarou
             return ResponseEntity.internalServerError().body(Map.of("erro", "Erro interno ao criar solicitação."));
        }
    }
}