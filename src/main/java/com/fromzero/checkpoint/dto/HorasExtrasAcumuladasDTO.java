package com.fromzero.checkpoint.dto;

import java.math.BigDecimal;

public class HorasExtrasAcumuladasDTO {
    private Long colaboradorId;
    private String colaboradorNome;
    private BigDecimal totalHoras;

    public HorasExtrasAcumuladasDTO() {
    }

    public HorasExtrasAcumuladasDTO(Long colaboradorId, String colaboradorNome, BigDecimal totalHoras) {
        this.colaboradorId = colaboradorId;
        this.colaboradorNome = colaboradorNome;
        this.totalHoras = totalHoras;
    }

    // Getters e Setters
    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public String getColaboradorNome() {
        return colaboradorNome;
    }

    public void setColaboradorNome(String colaboradorNome) {
        this.colaboradorNome = colaboradorNome;
    }

    public BigDecimal getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(BigDecimal totalHoras) {
        this.totalHoras = totalHoras;
    }
}