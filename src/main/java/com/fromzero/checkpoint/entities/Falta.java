package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Falta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Colaborador colaborador;

    public enum TipoFalta {
        Atraso,
        Ausencia
    }

    @Enumerated(EnumType.STRING)
    private TipoFalta tipo;
}
