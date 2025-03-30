package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.dto.AtualizarHorarioMarcacaoDTO;
import com.fromzero.checkpoint.dto.MarcacaoDTO;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.services.MarcacaoService;
import com.fromzero.checkpoint.services.NotificacaoService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/marcacoes")
public class MarcacaoController {

    @Autowired
    private MarcacaoService marcacaoService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // Listar todas as marcações
    @GetMapping()
    public List<Marcacao> listarMarcacoes() {
        return marcacaoService.listarMarcacoes();
    }

    // Buscar marcação por id
    @GetMapping("/{id}")
    public ResponseEntity<Marcacao> buscarMarcacaoPorId(@PathVariable String id) {
        return marcacaoService.buscarMarcacaoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Criar marcação
    @PostMapping()
    public ResponseEntity<Marcacao> criarMarcacao(@Valid @RequestBody MarcacaoDTO marcacaoDTO) {

    try {
        Marcacao.TipoMarcacao.valueOf(marcacaoDTO.getTipo().toString().toUpperCase());
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().build(); // Retorna 400 se o tipo for inválido
    }

    // Criando um novo objeto Marcacao a partir do DTO
    Marcacao novaMarcacao = new Marcacao();
    novaMarcacao.setColaboradorId(marcacaoDTO.getColaboradorId());
    novaMarcacao.setTipo(marcacaoDTO.getTipo());
    novaMarcacao.setProcessada(false); // Definir como não processada inicialmente

    // Salvando a nova marcação
    Marcacao marcacaoSalva = marcacaoService.criarMarcacao(novaMarcacao);
    return ResponseEntity.status(201).body(marcacaoSalva);
    }

    // Atualizar marcação
    @PutMapping("/{id}")
    public ResponseEntity<Marcacao> atualizarMarcacao(@PathVariable String id, @RequestBody Marcacao marcacaoAtualizada) {
        return ResponseEntity.ok(marcacaoService.atualizarMarcacao(id, marcacaoAtualizada));
    }

    // Deletar marcação
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMarcacao(@PathVariable String id) {
        marcacaoService.deletarMarcacao(id);
        return ResponseEntity.noContent().build();
    }

    // Obter marcações do dia atual de um colaborador específico
    @GetMapping("/colaborador/{colaboradorId}/hoje")
    public List<Marcacao> obterMarcacoesDoDia(@PathVariable Long colaboradorId) {
        return marcacaoService.obterMarcacoesDoDia(colaboradorId, LocalDate.now());
    }

    // Obter todas as marcações de um colaborador específico
    @GetMapping("/colaborador/{colaboradorId}")
    public List<Marcacao> obterTodasMarcacoesPorColaborador(@PathVariable Long colaboradorId) {
        return marcacaoService.obterTodasMarcacoesPorColaborador(colaboradorId);
    }

    // Obter marcações de um dia específico de um colaborador
    @GetMapping("/colaborador/{colaboradorId}/data/{data}")
    public List<Marcacao> obterMarcacoesPorData(
            @PathVariable Long colaboradorId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return marcacaoService.obterMarcacoesPorData(colaboradorId, data);
    }

    // Atualiza somente o horário
    @PutMapping("/{id}/horario")
    public ResponseEntity<Marcacao> atualizarHorarioMarcacao(
            @PathVariable String id,
            @Valid @RequestBody AtualizarHorarioMarcacaoDTO dto) {

        Marcacao marcacaoAtualizada = marcacaoService.atualizarHorarioMarcacao(id, dto.getNovoHorario());

        Colaborador colaborador = colaboradorRepository.findById(marcacaoAtualizada.getColaboradorId())
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        
        notificacaoService.criaNotificacao("O horário do seu ponto foi ajustado", NotificacaoTipo.ponto, colaborador);
        return ResponseEntity.ok(marcacaoAtualizada);
    }
}
