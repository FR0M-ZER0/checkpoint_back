package com.fromzero.checkpoint.dto;

import java.util.List;

public class MarcacoesPorDiaDTO {
    private String nome;
    private String status;
    private List<MarcacaoResumoDTO> marcacoes;

    public MarcacoesPorDiaDTO() {
    }

    public MarcacoesPorDiaDTO(String nome, String status, List<MarcacaoResumoDTO> marcacoes) {
        this.nome = nome;
        this.status = status;
        this.marcacoes = marcacoes;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MarcacaoResumoDTO> getMarcacoes() {
        return marcacoes;
    }

    public void setMarcacoes(List<MarcacaoResumoDTO> marcacoes) {
        this.marcacoes = marcacoes;
    }
}