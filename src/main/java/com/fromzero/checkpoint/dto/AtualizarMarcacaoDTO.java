package com.fromzero.checkpoint.dto;

import java.time.LocalDateTime;

import com.fromzero.checkpoint.entities.Marcacao;

import jakarta.validation.constraints.NotNull;

public class AtualizarMarcacaoDTO {

    @NotNull(message = "Nova data e hora são obrigatórias")
    private LocalDateTime novaDataHora;

    @NotNull(message = "Novo tipo é obrigatório")
    private Marcacao.TipoMarcacao novoTipo;

    public LocalDateTime getNovaDataHora() {
        return novaDataHora;
    }

    public void setNovaDataHora(LocalDateTime novaDataHora) {
        this.novaDataHora = novaDataHora;
    }

    public Marcacao.TipoMarcacao getNovoTipo() {
        return novoTipo;
    }

    public void setNovoTipo(Marcacao.TipoMarcacao novoTipo) {
        this.novoTipo = novoTipo;
    }
}
