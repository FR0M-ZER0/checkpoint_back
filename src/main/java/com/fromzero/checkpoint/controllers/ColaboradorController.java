package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.entities.Resposta;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.NotificacaoRepository;
import com.fromzero.checkpoint.repositories.RespostaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;
import com.fromzero.checkpoint.services.RespostaService;



@RestController
public class ColaboradorController {
    @Autowired
    private ColaboradorRepository repository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private RespostaService respostaService;

    @Autowired
    private RespostaRepository respostaRepository;

    @PostMapping("/colaborador")
    public Colaborador cadastrarColaborador(@RequestBody Colaborador c) {
        repository.save(c);
        return c;
    }

    @GetMapping("/colaborador")
    public List<Colaborador> obterColaboradores() {
        return repository.findAll();
    }

    @GetMapping("/colaborador/{id}")
    public Colaborador obterColaborador(@PathVariable Long id) {
        return repository.findById(id).get();
    }

    @PutMapping("/colaborador/{id}")
    public ResponseEntity<Colaborador> atualizarColaborador(@PathVariable Long id, @RequestBody Colaborador colaboradorAtualizado) {
        return repository.findById(id).map(colaborador -> {
            if (colaboradorAtualizado.getNome() != null) {
                colaborador.setNome(colaboradorAtualizado.getNome());
            }
            if (colaboradorAtualizado.getEmail() != null) {
                colaborador.setEmail(colaboradorAtualizado.getEmail());
            }
            if (colaboradorAtualizado.getSenhaHash() != null) {
                colaborador.setSenhaHash(colaboradorAtualizado.getSenhaHash());
            }
            if (colaboradorAtualizado.getAtivo() != null) {
                colaborador.setAtivo(colaboradorAtualizado.getAtivo());
            }
            if (colaboradorAtualizado.getSaldoFerias() != null) {
                colaborador.setSaldoFerias(colaboradorAtualizado.getSaldoFerias());
            }

            Colaborador atualizado = repository.save(colaborador);
            return ResponseEntity.ok(atualizado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/colaborador/buscar")
    public List<Colaborador> buscarPorNome(@RequestParam String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    @GetMapping("/colaborador/status")
    public List<Colaborador> buscarPorStatus(@RequestParam Boolean ativo) {
        return repository.findByAtivo(ativo);
    }

    @GetMapping("/colaborador/ordenar")
    public List<Colaborador> ordenarColaboradores(
            @RequestParam String campo,
            @RequestParam String ordem) {

        Sort.Direction direction = ordem.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        if (!campo.equals("nome") && !campo.equals("criadoEm")) {
            throw new IllegalArgumentException("Campo inválido para ordenação. Use 'nome' ou 'criadoEm'.");
        }

        return repository.findAll(Sort.by(direction, campo));
    }

    @GetMapping("/colaborador/faltas/{id}")
    public List<Falta> obterFaltasPorColaborador(@PathVariable Long id) {
        return faltaRepository.findByColaboradorId(id);
    }

    @GetMapping("/colaborador/falta/sem-solicitacao/{id}")
    public List<Falta> obterFaltasSemSolicitacao(@PathVariable Long id) {
        return faltaRepository.obterFaltasSemSolicitacao(id);
    }

    @GetMapping("/colaborador/notificacoes/{id}")
    public List<Notificacao> obterNotificacoesNaoLidas(@PathVariable Long id) {
        return notificacaoService.buscarNotificacoesNaoLidas(id);
    }

    @PutMapping("/colaborador/notificacao/{notificacaoId}")
    public Notificacao marcarNotificacaoComoLida(@PathVariable Long notificacaoId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);

        return notificacao;
    }

    @GetMapping("/colaborador/resposta/{id}")
    public List<Resposta> obterRespostasNaoLidas(@PathVariable Long id) {
        return respostaService.buscarRespostasNaoLidas(id);
    }

    @PutMapping("/colaborador/resposta/{respostaId}")
    public Resposta marcarRespostaComoLida(@PathVariable Long respostaId) {
        Resposta resposta = respostaRepository.findById(respostaId)
            .orElseThrow(() -> new RuntimeException("Resposta não encontrada"));

        resposta.setLida(true);
        respostaRepository.save(resposta);

        return resposta;
    }

    @PostMapping("/login")
    public ResponseEntity<?> logarColaborador(@RequestBody Colaborador c) {
        Colaborador colaborador = repository.findByEmail(c.getEmail()).orElseThrow(
            () -> new RuntimeException("Colaborador não encontrado")
        );

         if (colaborador.getSenhaHash().equals(c.getSenhaHash())) {
             return ResponseEntity.ok(colaborador);
         } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
         }
    }
}
