// Dentro de FolgaController.java
package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.entities.SolicitacaoFolga; // << IMPORTAR
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository; // << INJETAR
import com.fromzero.checkpoint.services.FolgaService; // Para o saldo
// import com.fromzero.checkpoint.entities.Folga; // Não mais necessário para o POST
// import com.fromzero.checkpoint.repositories.FolgaRepository; // Não mais necessário para o POST

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Para o status CREATED
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
// Adicione imports do Logger e NotificacaoService/ColaboradorRepository se for notificar
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import com.fromzero.checkpoint.services.NotificacaoService;
// import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
// import com.fromzero.checkpoint.entities.Colaborador;
// import com.fromzero.checkpoint.repositories.ColaboradorRepository;


@RestController
@RequestMapping("/api/folga") // O frontend chama esta URL base
@CrossOrigin(origins = "http://localhost:5173")
public class FolgaController {

    // private static final Logger log = LoggerFactory.getLogger(FolgaController.class);

    @Autowired
    private FolgaService folgaService; // Para o GET /saldo

    @Autowired
    private SolicitacaoFolgaRepository solicitacaoFolgaRepository; // Para GET e POST de solicitações

    // @Autowired
    // private FolgaRepository repository; // Não será usado para o POST de SolicitacaoFolga

    // Para enviar notificações WebSocket (se você usar a lógica do SolicitacaoFolgaController)
    // @Autowired
    // private SimpMessagingTemplate messagingTemplate;
    // @Autowired
    // private NotificacaoService notificacaoService;
    // @Autowired
    // private ColaboradorRepository colaboradorRepository;


    @GetMapping("/saldo")
    public ResponseEntity<String> getSaldoHoras(@RequestParam Long colaboradorId) {
        try {
            String saldo = folgaService.obterSaldoHoras(colaboradorId);
            return ResponseEntity.ok(saldo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET para listar as solicitações de folga do colaborador
    @GetMapping
    public ResponseEntity<?> getSolicitacoesDeFolgaPorColaborador(@RequestParam Long colaboradorId) {
        // log.info("FolgaController: Solicitadas folgas para Colaborador ID: {}", colaboradorId);
        try {
            List<SolicitacaoFolga> solicitacoes = solicitacaoFolgaRepository.findByColaboradorId(colaboradorId);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            // log.error("Erro ao buscar solicitações de folga para colaborador ID: {}", colaboradorId, e);
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro ao buscar histórico de folgas."));
        }
    }

    // ***** MÉTODO @PostMapping CORRIGIDO PARA SolicitacaoFolga *****
    @PostMapping // Responde a POST /api/folga
    public ResponseEntity<?> createSolicitacaoFolga(@RequestBody SolicitacaoFolga solicitacao) {
        // log.info("FolgaController: Recebida nova solicitação de folga: {}", solicitacao);
        try {
            // Validações básicas
            if (solicitacao.getColaboradorId() == null || 
                solicitacao.getSolFolData() == null || 
                solicitacao.getSolFolSaldoGasto() == null || solicitacao.getSolFolSaldoGasto().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Campos obrigatórios da solicitação faltando."));
            }
             if (solicitacao.getSolFolStatus() == null || solicitacao.getSolFolStatus().isBlank()) {
                solicitacao.setSolFolStatus("PENDENTE"); // Garante o status padrão
            }
            // Poderia adicionar validação de saldo aqui chamando folgaService.obterSaldoHoras se necessário

            SolicitacaoFolga solicitacaoSalva = solicitacaoFolgaRepository.save(solicitacao);

            // Lógica de notificação (adaptada do seu SolicitacaoFolgaController)
            // Colaborador colaborador = colaboradorRepository.findById(solicitacaoSalva.getColaboradorId())
            //     .orElse(null); // Tratar caso não encontre
            // if (colaborador != null) {
            //     notificacaoService.criaNotificacao(
            //         "Sua solicitação de folga foi enviada e está pendente.",
            //         NotificacaoTipo.folga, // Certifique-se que NotificacaoTipo tem 'folga'
            //         colaborador
            //     );
            // }
            // messagingTemplate.convertAndSend("/topic/solicitacoesFolga", solicitacaoSalva); // Se tiver um tópico específico

            return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoSalva);
        } catch (Exception e) {
            // log.error("Erro ao criar solicitação de folga:", e);
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro interno ao processar solicitação de folga."));
        }
    }
}