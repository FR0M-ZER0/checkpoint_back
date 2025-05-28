package com.fromzero.checkpoint.dto;

public class UltimaMarcacaoResumoDTO {
    private String nome;
    private String entrada;
    private String pausa;
    private String retomada;
    private String saida;
    private String total;

    public UltimaMarcacaoResumoDTO() {}

    public UltimaMarcacaoResumoDTO(String nome, String entrada, String pausa, String retomada, String saida, String total) {
        this.nome = nome;
        this.entrada = entrada;
        this.pausa = pausa;
        this.retomada = retomada;
        this.saida = saida;
        this.total = total;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEntrada() { return entrada; }
    public void setEntrada(String entrada) { this.entrada = entrada; }

    public String getPausa() { return pausa; }
    public void setPausa(String pausa) { this.pausa = pausa; }

    public String getRetomada() { return retomada; }
    public void setRetomada(String retomada) { this.retomada = retomada; }

    public String getSaida() { return saida; }
    public void setSaida(String saida) { this.saida = saida; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }
}
