package com.fromzero.checkpoint.dto;

public class MarcacaoResumoDTO {
    private String tipo;
    private String horario;

    public MarcacaoResumoDTO() {}

    public MarcacaoResumoDTO(String tipo, String horario) {
        this.tipo = tipo;
        this.horario = horario;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
}
