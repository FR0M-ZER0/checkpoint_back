package com.fromzero.checkpoint.dto;

import com.fromzero.checkpoint.entities.Marcacao.TipoMarcacao;

import jakarta.validation.constraints.NotNull;

public class MarcacaoDTO {
    
    @NotNull(message = "ID do colaborador é obrigatório")
    private Long colaboradorId; // id do colaborador

    @NotNull(message = "Tipo da marcação é obrigatório")
    private TipoMarcacao tipo; // tipo da marcação ("ENTRADA" ou "SAIDA")

    public MarcacaoDTO() {
    }

    public MarcacaoDTO(Long colaboradorId, TipoMarcacao tipo) {
        this.colaboradorId = colaboradorId;
        this.tipo = tipo;
    }
    
    // getters e setters    
    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public TipoMarcacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMarcacao tipo) {
        this.tipo = tipo;
    }
}
