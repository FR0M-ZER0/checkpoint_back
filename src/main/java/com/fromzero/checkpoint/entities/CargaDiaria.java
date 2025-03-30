package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
public class CargaDiaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String diaInicio;

    @Column(nullable = true)
    private String diaFinal;

    @Column
    private LocalTime entrada;

    @Column
    private LocalTime pausa;

    @Column
    private LocalTime retomada;

    @Column
    private LocalTime fim;

    @Column
    private LocalDateTime criadoEm;

    @ManyToOne()
    @JoinColumn(name = "jornada_id")
    private Jornada jornada;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }
}