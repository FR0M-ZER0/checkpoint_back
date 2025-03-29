package com.fromzero.checkpoint.dto;

import java.util.List;

public class MesHistorico {
    private String mes;
    private List<DiaHistorico> dias;

    // Getters e Setters
    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public List<DiaHistorico> getDias() {
        return dias;
    }

    public void setDias(List<DiaHistorico> dias) {
        this.dias = dias;
    }
}