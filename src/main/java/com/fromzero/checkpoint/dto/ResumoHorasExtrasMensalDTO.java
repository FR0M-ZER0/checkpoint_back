package com.fromzero.checkpoint.dto;

import java.math.BigDecimal;

public class ResumoHorasExtrasMensalDTO {
    private BigDecimal totalMesAtual;
    private BigDecimal diferencaEmHoras;

    public ResumoHorasExtrasMensalDTO(BigDecimal totalMesAtual, BigDecimal diferencaEmHoras) {
        this.totalMesAtual = totalMesAtual;
        this.diferencaEmHoras = diferencaEmHoras;
    }

    public BigDecimal getTotalMesAtual() {
        return totalMesAtual;
    }

    public void setTotalMesAtual(BigDecimal totalMesAtual) {
        this.totalMesAtual = totalMesAtual;
    }

    public BigDecimal getDiferencaEmHoras() {
        return diferencaEmHoras;
    }

    public void setDiferencaEmHoras(BigDecimal diferencaEmHoras) {
        this.diferencaEmHoras = diferencaEmHoras;
    }
}
