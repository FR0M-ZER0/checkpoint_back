package com.fromzero.checkpoint.services;

// ***** IMPORTS IMPORTANTES *****
import com.fromzero.checkpoint.entities.*; // Inclui Notificacao, Colaborador, SolicitacaoFerias, etc.
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo; // Importa o Enum aninhado (CONFIRME ESTE CAMINHO/NOME)
// ******************************
import com.fromzero.checkpoint.repositories.*; // Inclui NotificacaoRepository, etc.
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.fromzero.checkpoint.dto.NotificacaoPayloadDTO; // Importe seu DTO
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import para o helper de data
import java.util.List;
import java.util.Objects;
import java.time.temporal.ChronoUnit;


@Service
public class FeriasService {

    private static final Logger log = LoggerFactory.getLogger(FeriasService.class);

    // --- Injeções de Dependência ---
    @Autowired private ColaboradorRepository colaboradorRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private AbonoFeriasRepository solicitacaoAbonoFeriasRepository;
    @Autowired private FeriasRepository feriasRepository;
    @Autowired private SolicitacaoFeriasRepository solicitacaoFeriasRepository;
    // Garanta que NotificacaoRepository está injetado
    @Autowired private NotificacaoRepository notificacaoRepository; 
    // -----------------------------

    // --- Constantes ---
    private static final int LIMITE_DIAS_VENDA_ANO = 10;
    private static final String STATUS_VENDA_APROVADA = "APROVADO"; // Confirme este valor
    // ------------------

    // --- Buscar Solicitações por Status (Paginado) ---
    public Page<SolicitacaoFerias> buscarSolicitacoesPorStatus(String status, Pageable pageable) {
        log.info("Buscando solicitações de férias com status: {} - Página: {}, Tamanho: {}", status, pageable.getPageNumber(), pageable.getPageSize());
        // Chama o método do repositório com JOIN FETCH para carregar o colaborador
        Page<SolicitacaoFerias> paginaSolicitacoes = solicitacaoFeriasRepository.findByStatusIgnoreCaseWithColaborador(status, pageable);
        log.info("Encontradas {} solicitações na página {} (Total: {}).", paginaSolicitacoes.getNumberOfElements(), pageable.getPageNumber(), paginaSolicitacoes.getTotalElements());
        return paginaSolicitacoes;
    }

    // --- Obter Saldo de Férias ---
    public Double obterSaldoFerias(Long colaboradorId) {
        log.info("Buscando saldo de férias para colaborador ID: {}", colaboradorId);
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new EntityNotFoundException("Colaborador com ID " + colaboradorId + " não encontrado"));
        if (colaborador.getSaldoFerias() == null) {
            log.warn("Saldo de férias NULO para o colaborador ID: {}. Retornando 0.0", colaboradorId);
             return 0.0; // Ou lance exceção, dependendo da regra
        }
        log.info("Saldo encontrado para colaborador ID {}: {}", colaboradorId, colaborador.getSaldoFerias());
        return colaborador.getSaldoFerias();
    }

    // --- Buscar Solicitações por Colaborador ---
    // RECOMENDAÇÃO: Criar e usar um método com JOIN FETCH aqui também se precisar do comentarioGestor no frontend Ferias.tsx
    public List<SolicitacaoFerias> buscarSolicitacoesPorColaborador(Long colaboradorId) {
        log.info("Buscando todas as solicitações de férias para colaborador ID: {}", colaboradorId);
        List<SolicitacaoFerias> solicitacoes = solicitacaoFeriasRepository.findByColaboradorId(colaboradorId);
        log.info("Encontradas {} solicitações para o colaborador {}.", solicitacoes.size(), colaboradorId);
        return solicitacoes;
    }

    // --- Vender Férias (Abono) ---
    @Transactional
    public SolicitacaoAbonoFerias venderFerias(SolicitacaoAbonoFerias solicitacao) {
        // ... (Lógica completa do venderFerias como estava antes) ...
         log.info("Iniciando processo de venda de férias: {}", solicitacao);
         // Validações...
         Colaborador colaborador = /* ... busca colaborador ... */ null;
         // Validação limite 10 dias...
         // Validação saldo...
         // Atualiza saldo...
         // Salva colaborador...
         SolicitacaoAbonoFerias abonoSalvo = solicitacaoAbonoFeriasRepository.save(solicitacao);
         log.info("Solicitação de venda salva com sucesso: {}", abonoSalvo);
         // TODO: Enviar notificação/salvar registro aqui também se necessário?
         return abonoSalvo;
    }


    // --- Agendar Férias (Criar Solicitação) ---
    @Transactional
    public SolicitacaoFerias agendarFerias(SolicitacaoFerias solicitacao) {
        // ... (Lógica completa do agendarFerias como estava antes) ...
         log.info("Iniciando processo de SOLICITAÇÃO de férias: {}", solicitacao);
         // Validações...
         // Verifica colaborador...
         SolicitacaoFerias solicitacaoSalva = solicitacaoFeriasRepository.save(solicitacao);
         log.info("Solicitação de férias salva com sucesso: {}", solicitacaoSalva);
         return solicitacaoSalva;
    }

    // --- REJEITAR Solicitação de Férias ---
    @Transactional
    public SolicitacaoFerias rejeitarSolicitacao(Long solicitacaoId, String comentario) {
        log.info("Rejeitando solicitação ID: {}", solicitacaoId);
        // Busca com JOIN FETCH para ter o Colaborador carregado
        SolicitacaoFerias solicitacao = solicitacaoFeriasRepository.findByIdWithColaborador(solicitacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação de férias com ID " + solicitacaoId + " não encontrada."));

        if (!"PENDENTE".equalsIgnoreCase(solicitacao.getStatus())) { 
            throw new IllegalStateException("Só é possível rejeitar solicitações com status PENDENTE.");
        }
        if (comentario == null || comentario.trim().isEmpty()) { 
             log.warn("Rejeitando solicitação {} sem comentário (verificar regra).", solicitacaoId);
             // throw new IllegalArgumentException("Comentário é obrigatório para rejeitar.");
        }

        solicitacao.setStatus("REJEITADO"); // Define status
        solicitacao.setComentarioGestor(comentario); // Define comentário do gestor (campo precisa existir na entidade)

        SolicitacaoFerias solicitacaoSalva = solicitacaoFeriasRepository.save(solicitacao); // Salva atualização
        log.info("Solicitação ID {} rejeitada e salva.", solicitacaoId);

        // ***** ENVIAR E SALVAR NOTIFICAÇÃO *****
        try {
            String mensagem = "Sua solicitação de férias (" + formatDateForNotification(solicitacao.getDataInicio()) + " a " + formatDateForNotification(solicitacao.getDataFim()) + ") foi REJEITADA.";
            
            // **** Cria e Salva a Notificação no Banco ****
            Notificacao novaNotificacao = new Notificacao();
            novaNotificacao.setMensagem(mensagem);
            novaNotificacao.setTipo(NotificacaoTipo.ferias); // Usa o Enum (confirme nome/valor)
            novaNotificacao.setColaborador(solicitacao.getColaborador()); // Usa o objeto Colaborador
            novaNotificacao.setLida(false);
            notificacaoRepository.save(novaNotificacao); 
            log.info("Notificação de rejeição (ID DB: {}) salva no banco para colaborador {}", novaNotificacao.getId(), solicitacao.getColaboradorId());
            // *********************************************

            // Envio via WebSocket
            NotificacaoPayloadDTO notificacaoPayload = new NotificacaoPayloadDTO("ferias_rejeitada", mensagem, solicitacaoSalva.getId());
            String destination = "/queue/notifications"; 
            messagingTemplate.convertAndSendToUser(solicitacao.getColaboradorId().toString(), destination, notificacaoPayload);
            log.info("Notificação de rejeição enviada para colaborador {} via WebSocket.", solicitacao.getColaboradorId());

        } catch (Exception e) {
            log.error("Falha ao salvar ou enviar notificação de rejeição para solicitação ID {}", solicitacaoId, e);
        }
        // ******************************************

        return solicitacaoSalva;
    }

    // --- APROVAR Solicitação de Férias ---
     @Transactional
     public SolicitacaoFerias aprovarSolicitacao(Long solicitacaoId, String comentario) {
         log.info("Aprovando solicitação ID: {}", solicitacaoId);
         SolicitacaoFerias solicitacao = solicitacaoFeriasRepository.findByIdWithColaborador(solicitacaoId) // Usa JOIN FETCH
                 .orElseThrow(() -> new EntityNotFoundException("Solicitação de férias com ID " + solicitacaoId + " não encontrada."));

        if (!"PENDENTE".equalsIgnoreCase(solicitacao.getStatus())) { 
             throw new IllegalStateException("Só é possível aprovar solicitações com status PENDENTE.");
        }

         Colaborador colaborador = solicitacao.getColaborador(); // Objeto já carregado
         long diasSolicitados = ChronoUnit.DAYS.between(solicitacao.getDataInicio(), solicitacao.getDataFim()) + 1;

         // *** VERIFICA E ATUALIZA SALDO ***
         if (colaborador.getSaldoFerias() == null || colaborador.getSaldoFerias() < diasSolicitados) { 
              throw new IllegalStateException("Saldo de férias insuficiente ("+ colaborador.getSaldoFerias() + ") para aprovar " + diasSolicitados + " dias.");
         }
         Double novoSaldo = colaborador.getSaldoFerias() - diasSolicitados;
         colaborador.setSaldoFerias(novoSaldo);
         colaboradorRepository.save(colaborador);
         log.info("Saldo do colaborador ID {} atualizado para: {}", colaborador.getId(), novoSaldo);
         // *******************************

         solicitacao.setStatus("APROVADO");
         solicitacao.setComentarioGestor(comentario); // Salva comentário

         SolicitacaoFerias solicitacaoSalva = solicitacaoFeriasRepository.save(solicitacao);
         log.info("Solicitação ID {} aprovada e salva.", solicitacaoId);

         // ***** ENVIAR E SALVAR NOTIFICAÇÃO *****
         try {
             String mensagem = "Sua solicitação de férias (" + formatDateForNotification(solicitacao.getDataInicio()) + " a " + formatDateForNotification(solicitacao.getDataFim()) + ") foi APROVADA.";
             
             // **** Cria e Salva a Notificação no Banco ****
             Notificacao novaNotificacao = new Notificacao();
             novaNotificacao.setMensagem(mensagem);
             novaNotificacao.setTipo(NotificacaoTipo.ferias); // Usa o Enum (confirme nome/valor)
             novaNotificacao.setColaborador(solicitacao.getColaborador()); // Usa o objeto Colaborador
             novaNotificacao.setLida(false);
             notificacaoRepository.save(novaNotificacao); 
             log.info("Notificação de aprovação (ID DB: {}) salva no banco para colaborador {}", novaNotificacao.getId(), solicitacao.getColaboradorId());
             // ********************************************

             // Envio via WebSocket
             NotificacaoPayloadDTO notificacaoPayload = new NotificacaoPayloadDTO("ferias_aprovada", mensagem, solicitacaoSalva.getId());
             String destination = "/queue/notifications";
             messagingTemplate.convertAndSendToUser( solicitacao.getColaboradorId().toString(), destination, notificacaoPayload);
             log.info("Notificação de aprovação enviada para colaborador {} via WebSocket.", solicitacao.getColaboradorId());
         } catch (Exception e) {
              log.error("Falha ao salvar ou enviar notificação de aprovação para solicitação ID {}", solicitacaoId, e);
         }
        // ******************************************

         // TODO: Criar registro na tabela Ferias?

         return solicitacaoSalva;
     }

      // Helper para formatar data
      private String formatDateForNotification(LocalDate date) {
          if (date == null) return "??";
          return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      }
      
} // Fim da classe FeriasService