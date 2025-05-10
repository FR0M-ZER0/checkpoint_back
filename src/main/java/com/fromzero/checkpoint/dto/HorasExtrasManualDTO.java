package com.fromzero.checkpoint.dto;

public class HorasExtrasManualDTO {

    private Long colaboradorId;
    private Long gestorId;
    private String saldo;  // Ex: "1.50"
    private String status; // Ex: "Aprovado"
    private String tipo;   // Ex: "adicao", "edicao", "exclusao"
    private String justificativa; // Ex: "Justificativa para a alteração"

    public HorasExtrasManualDTO() {}

    public HorasExtrasManualDTO(Long colaboradorId, Long gestorId, String saldo, String status, String tipo) {
        this.colaboradorId = colaboradorId;
        this.gestorId = gestorId;
        this.saldo = saldo;
        this.status = status;
        this.tipo = tipo;
        this.justificativa = justificativa;
    }

    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public Long getGestorId() {
        return gestorId;
    }

    public void setGestorId(Long gestorId) {
        this.gestorId = gestorId;
    }

    public String getSaldo() {
        return saldo;
    }

    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

}