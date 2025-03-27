package com.fromzero.checkpoint.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "marcacao_log")
public class MarcacaoLog {

    @Id
    private String id;

    private Long colaboradorId;
    private String acao; // Ex: "CRIACAO", "DELECAO"
    private Marcacao.TipoMarcacao tipoMarcacao;
    private LocalDateTime dataHora = LocalDateTime.now();

    // Construtores
    public MarcacaoLog() {}

    public MarcacaoLog(Long colaboradorId, String acao, Marcacao.TipoMarcacao tipoMarcacao) {
        this.colaboradorId = colaboradorId;
        this.acao = acao;
        this.tipoMarcacao = tipoMarcacao;
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public Marcacao.TipoMarcacao getTipoMarcacao() {
        return tipoMarcacao;
    }

    public void setTipoMarcacao(Marcacao.TipoMarcacao tipoMarcacao) {
        this.tipoMarcacao = tipoMarcacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "MarcacaoLog{" +
                "colaboradorId=" + colaboradorId +
                ", acao='" + acao + '\'' +
                ", tipoMarcacao=" + tipoMarcacao +
                ", dataHora=" + dataHora +
                '}';
    }
}
