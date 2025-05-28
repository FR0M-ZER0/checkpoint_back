package com.fromzero.checkpoint.dto;

import com.fromzero.checkpoint.entities.Marcacao.TipoMarcacao;
import java.time.LocalDateTime;

public class MarcacaoResponseDTO {
    
    private String id;
    private Long colaboradorId;
    private String nomeColaborador;
    private TipoMarcacao tipo;
    private LocalDateTime dataHora;
    private boolean processada;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Long getColaboradorId() {
        return colaboradorId;
    }
    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }
    public String getNomeColaborador() {
        return nomeColaborador;
    }
    public void setNomeColaborador(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }
    public TipoMarcacao getTipo() {
        return tipo;
    }
    public void setTipo(TipoMarcacao tipo) {
        this.tipo = tipo;
    }
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    public boolean isProcessada() {
        return processada;
    }
    public void setProcessada(boolean processada) {
        this.processada = processada;
    }
}
