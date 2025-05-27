package com.fromzero.checkpoint.dto;

import java.time.LocalDateTime;

import com.fromzero.checkpoint.entities.Falta;

import lombok.Data;

@Data
public class FaltaComColaboradorDTO {
    private Long id;
    private Falta.TipoFalta tipo;
    private Boolean justificado;
    private String nomeColaborador;
    private LocalDateTime criadoEm;

    public FaltaComColaboradorDTO(Falta falta) {
        this.id = falta.getId();
        this.tipo = falta.getTipo();
        this.justificado = falta.getJustificado();
        this.nomeColaborador = falta.getColaborador().getNome();
        this.criadoEm = falta.getCriadoEm();
    }
}
