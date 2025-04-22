package com.fromzero.checkpoint.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fromzero.checkpoint.services.FeriasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import jakarta.persistence.EntityNotFoundException;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.FeriasRepository;
// ***** IMPORTS CORRIGIDOS/ADICIONADOS *****
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFerias;
import com.fromzero.checkpoint.entities.SolicitacaoFerias; // Precisa desta entidade!
// Ferias não é mais necessário como tipo de retorno ou parâmetro para /agendar
import java.util.List;
// import com.fromzero.checkpoint.entities.Ferias;
import java.util.Map;
// *****************************************

@RestController
@RequestMapping("/api/ferias")
@CrossOrigin(origins = "http://localhost:5173") // Verifique se esta origem está correta
public class FeriasController {

    @Autowired
    private FeriasService feriasService;

    @Autowired
    private FeriasRepository repository;

    @GetMapping("/saldo")
    public ResponseEntity<?> getSaldoFerias(@RequestParam Long colaboradorId) {
        try {
            // Validação básica do ID (opcional, service também pode fazer)
            if (colaboradorId == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "ID do colaborador é obrigatório"));
            }

            Double saldo = feriasService.obterSaldoFerias(colaboradorId);
            return ResponseEntity.ok(saldo);

        } catch (RuntimeException e) {
            HttpStatus status = (e instanceof jakarta.persistence.EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
             return ResponseEntity.status(status).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            // Logar o erro completo aqui é importante
            // log.error("Erro inesperado ao buscar saldo", e);
            return ResponseEntity.internalServerError()
                         .body(Map.of("erro", "Erro interno ao processar requisição de saldo."));
        }
    }

    @PutMapping("/solicitacoes/{id}/rejeitar")
    public ResponseEntity<?> rejeitarSolicitacao(
            @PathVariable Long id, // Pega o ID da URL
            @RequestBody(required = false) Map<String, String> body // Recebe o corpo (comentario)
    ) {
        try {
            String comentario = (body != null) ? body.get("comentarioGestor") : null;

            // Chama o método do service para rejeitar
            // Idealmente, o service retorna a solicitação atualizada
            SolicitacaoFerias solicitacaoRejeitada = feriasService.rejeitarSolicitacao(id, comentario);
            return ResponseEntity.ok(solicitacaoRejeitada); // Retorna 200 OK com a solicitação atualizada

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        } catch (IllegalArgumentException e) { // Ex: Comentário obrigatório não fornecido
                return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            // log.error("Erro ao rejeitar solicitação {}", id, e);
                return ResponseEntity.internalServerError().body(Map.of("erro", "Erro interno ao rejeitar solicitação."));
        }
    }

    @PostMapping("/vender")
    // Usar ResponseEntity<?> para flexibilidade no retorno (sucesso ou erro JSON)
    public ResponseEntity<?> venderFerias(@RequestBody SolicitacaoAbonoFerias abono) { // Tipo correto: SolicitacaoAbonoFerias
        try {
            SolicitacaoAbonoFerias abonoVendido = feriasService.venderFerias(abono);
            return ResponseEntity.ok(abonoVendido); // Retorna o objeto salvo em caso de sucesso
        } catch (RuntimeException e) { // Captura erros de validação ou outros do service
            // **** CATCH CORRIGIDO ****
            Map<String, String> errorResponse = Map.of("erro", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse); // Retorna 400 com JSON de erro
        } catch (Exception e) {
             // log.error("Erro inesperado ao vender férias", e);
             return ResponseEntity.internalServerError()
                          .body(Map.of("erro", "Erro interno ao processar requisição de venda."));
        }
    }


    @PostMapping("/agendar")
    // ***** ASSINATURA CORRIGIDA *****
    public ResponseEntity<?> agendarFerias(@RequestBody SolicitacaoFerias solicitacao) { // Recebe SolicitacaoFerias
        try {
            // ***** CHAMADA E VARIÁVEL CORRIGIDAS *****
            // Chama o service passando SolicitacaoFerias, espera SolicitacaoFerias de volta
            SolicitacaoFerias solicitacaoSalva = feriasService.agendarFerias(solicitacao);
            return ResponseEntity.ok(solicitacaoSalva); // Retorna a solicitação salva
        } catch (RuntimeException e) { // Captura erros de validação ou outros do service
            // **** CATCH CORRIGIDO ****
            Map<String, String> errorResponse = Map.of("erro", e.getMessage());
            // Retorna 404 se for EntityNotFound, 400 para outros erros de validação/negócio
            HttpStatus status = (e instanceof jakarta.persistence.EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
             // log.error("Erro inesperado ao agendar férias", e);
             return ResponseEntity.internalServerError()
                          .body(Map.of("erro", "Erro interno ao processar solicitação de agendamento."));
        }
    }
    @GetMapping("/solicitacoes")
    public ResponseEntity<?> buscarSolicitacoes(
            @RequestParam(required = false, defaultValue = "PENDENTE") String status,
            // ***** 1. ADICIONA Pageable COMO PARÂMETRO *****
            // O Spring vai preencher isso com ?page=X&size=Y&sort=Z da URL
            // @PageableDefault define valores padrão se não vierem na URL
            @PageableDefault(size = 12, sort = "id") Pageable pageable 
    ) {
        try {

            Page<SolicitacaoFerias> paginaSolicitacoes = feriasService.buscarSolicitacoesPorStatus(status, pageable); 
            return ResponseEntity.ok(paginaSolicitacoes);
        } catch (Exception e) {
            // log.error("Erro ao buscar solicitações de férias", e); 
            Map<String, String> errorResponse = Map.of("erro", "Erro interno ao buscar solicitações de férias.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    @GetMapping("/solicitacoes/colaborador/{colaboradorId}")
    public ResponseEntity<?> buscarMinhasSolicitacoes(
            @PathVariable Long colaboradorId
    ) {
        try {
            List<SolicitacaoFerias> solicitacoes = feriasService.buscarSolicitacoesPorColaborador(colaboradorId);
            return ResponseEntity.ok(solicitacoes);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of("erro", "Erro interno ao buscar histórico de solicitações.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping
    public Ferias createFerias(@RequestBody Ferias f) {
        return repository.save(f);
    }
}