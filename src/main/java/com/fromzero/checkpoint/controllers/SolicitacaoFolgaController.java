package com.fromzero.checkpoint.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.SolicitacaoFolga;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoFolgaRepository;

@RestController
public class SolicitacaoFolgaController {
	@Autowired
	private SolicitacaoFolgaRepository repository;
	
	@Autowired
	private ColaboradorRepository colaboradorRepository;
	
	@PostMapping("/solicitacao-folga")
    public SolicitacaoFolga cadastrarSolicitacaoFolga(@RequestBody SolicitacaoFolga f) {
        Colaborador colaborador = colaboradorRepository.findById(f.getColaborador().getId())
            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
        f.setColaborador(colaborador);
        repository.save(f);  
        return f;
    }
	
    @GetMapping("/solicitacao-folga")
    public List<SolicitacaoFolga> obterFaltas() {
        return repository.findAll();
    }
    
    @GetMapping("/solicitacao-folga/{id}")
    public SolicitacaoFolga obterFalta(@PathVariable Long id) {
        return repository.findById(id).get();
    }
    
    @PutMapping("/solicitacao-folga/{id}")
    public SolicitacaoFolga atualizarSolicitacaoFolga(@PathVariable Long id, @RequestBody SolicitacaoFolga solicitacaoAtualizada) {
        
        
        SolicitacaoFolga f = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
        f.setSolFolData(solicitacaoAtualizada.getSolFolData());
        return repository.save(f);
    }
    
    @DeleteMapping("/solicitacao-folga/{id}")
    public void deletarSolicitacaoFolga(@PathVariable Long id) {
        repository.deleteById(id); // Uses JpaRepository's built-in delete method [[5]]
    }
}
