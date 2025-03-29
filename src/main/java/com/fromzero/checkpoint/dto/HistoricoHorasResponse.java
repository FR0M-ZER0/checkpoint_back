package com.fromzero.checkpoint.dto;

import java.util.List;

public class HistoricoHorasResponse {
    private int ano;
    private List<MesHistorico> meses;
    private List<Integer> anosDisponiveis;

    // Getters e Setters
    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public List<MesHistorico> getMeses() {
        return meses;
    }

    public void setMeses(List<MesHistorico> meses) {
        this.meses = meses;
    }

    public List<Integer> getAnosDisponiveis() {
        return anosDisponiveis;
    }

    public void setAnosDisponiveis(List<Integer> anosDisponiveis) {
        this.anosDisponiveis = anosDisponiveis;
    }
}