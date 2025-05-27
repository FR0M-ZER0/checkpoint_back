package com.fromzero.checkpoint.dto;

import java.time.LocalDateTime;

import com.fromzero.checkpoint.entities.Marcacao;

public class MarcacaoComDataDTO {
    private Long colaboradorId;
    private Marcacao.TipoMarcacao tipo;
    private LocalDateTime dataHora;

    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public Marcacao.TipoMarcacao getTipo() {
        return tipo;
    }

    public void setTipo(Marcacao.TipoMarcacao tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
