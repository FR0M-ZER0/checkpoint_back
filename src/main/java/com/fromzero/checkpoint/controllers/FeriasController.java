package com.fromzero.checkpoint.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fromzero.checkpoint.services.FeriasService;

// ***** IMPORTS CORRIGIDOS/ADICIONADOS *****
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFerias;
import com.fromzero.checkpoint.entities.SolicitacaoFerias; // Precisa desta entidade!
// Ferias não é mais necessário como tipo de retorno ou parâmetro para /agendar
// import com.fromzero.checkpoint.entities.Ferias;
import java.util.Map;
// *****************************************

@RestController
@RequestMapping("/api/ferias")
@CrossOrigin(origins = "http://localhost:5173") // Verifique se esta origem está correta
public class FeriasController {

    @Autowired
    private FeriasService feriasService;

    @GetMapping("/saldo")
    public ResponseEntity<?> getSaldoFerias(@RequestParam Long colaboradorId) {
        try {
            // Validação básica do ID (opcional, service também pode fazer)
            if (colaboradorId == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "ID do colaborador é obrigatório"));
            }

            Double saldo = feriasService.obterSaldoFerias(colaboradorId);
            return ResponseEntity.ok(saldo);

        } catch (RuntimeException e) { // Captura EntityNotFound ou IllegalState do service
            // Retorna 404 ou 400 dependendo do erro, com mensagem JSON
            HttpStatus status = (e instanceof jakarta.persistence.EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
             return ResponseEntity.status(status).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            // Logar o erro completo aqui é importante
            // log.error("Erro inesperado ao buscar saldo", e);
            return ResponseEntity.internalServerError()
                         .body(Map.of("erro", "Erro interno ao processar requisição de saldo."));
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

}