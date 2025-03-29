package com.fromzero.checkpoint.dto;

import java.math.BigDecimal;

public class HorasExtrasDTO {

    private String mesAno; // Ex: "Agosto/2025"
    private BigDecimal totalHoras;

    // Construtor padrão
    public HorasExtrasDTO() {
    }

    // Construtor com parâmetros
    public HorasExtrasDTO(String mesAno, BigDecimal totalHoras) {
        this.mesAno = mesAno;
        this.totalHoras = totalHoras;
    }

    public String getMesAno() {
        return mesAno;
    }

    public void setMesAno(String mesAno) {
        this.mesAno = mesAno;
    }

    public BigDecimal getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(BigDecimal totalHoras) {
        this.totalHoras = totalHoras;
    }
}
