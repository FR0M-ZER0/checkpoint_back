package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.dto.MarcacaoDTO;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.services.MarcacaoService;

import jakarta.validation.Valid;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/marcacoes")
public class MarcacaoController {

    @Autowired
    private MarcacaoService marcacaoService;

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

    // Novo endpoint: cálculo de horas trabalhadas hoje
    @GetMapping("/horas-trabalhadas/{colaboradorId}")
    public ResponseEntity<Map<String, Object>> calcularHorasTrabalhadasHoje(@PathVariable Long colaboradorId) {
        Duration duracao = marcacaoService.calcularHorasTrabalhadasHoje(colaboradorId);
        String resultado = duracao.toHours() + "h: " + duracao.toMinutesPart() + "min";

        Map<String, Object> response = new HashMap<>();
        response.put("colaboradorId", colaboradorId);
        response.put("horasTrabalhadas", resultado);

        return ResponseEntity.ok(response);
    }

    // Novo endpoint: cálculo de horas trabalhadas por mês
    // Total de horas trabalhadas por mês
    @GetMapping("/horas-trabalhadas/mensal/{colaboradorId}")
    public ResponseEntity<Map<String, String>> calcularHorasTrabalhadasPorMes(@PathVariable Long colaboradorId) {
        Map<String, String> resultado = marcacaoService.calcularHorasTrabalhadasPorMes(colaboradorId);
        return ResponseEntity.ok(resultado);
    }
}
