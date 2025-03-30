package com.fromzero.checkpoint.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ferias")
public class Ferias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false)
    private Integer diasUteis;

    @Column(nullable = false)
    private String status;

    @Transient
    private List<LocalDate> feriados; // Lista de feriados (pode ser injetado ou buscado de um serviço)

    @PrePersist
    @PreUpdate
    public void calcularDiasUteis() {
        if (dataInicio != null && dataFim != null) {
            this.diasUteis = calcularDiasUteisEntreDatas(dataInicio, dataFim);
        }
    }

    private int calcularDiasUteisEntreDatas(LocalDate inicio, LocalDate fim) {
        int diasUteis = 0;
        LocalDate data = inicio;

        while (!data.isAfter(fim)) {
            if (!ehFimDeSemana(data) && !ehFeriado(data)) {
                diasUteis++;
            }
            data = data.plusDays(1);
        }

        return diasUteis;
    }

    private boolean ehFimDeSemana(LocalDate data) {
        return data.getDayOfWeek().getValue() >= 6; // 6 = sábado, 7 = domingo
    }

    private boolean ehFeriado(LocalDate data) {
        if (feriados == null) return false;
        return feriados.contains(data);
    }

    public boolean isPeriodoValido() {
        return ChronoUnit.DAYS.between(dataInicio, dataFim) >= 14;
    }
}