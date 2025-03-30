package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "faltas")
public class Falta {
    public enum TipoFalta {
        ATRASO, AUSENCIA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime criadoEm;

    @Column
    private LocalDate data;

    @Column
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne
    @JoinColumn(name = "jornada_id")
    private Jornada jornada;

    @Enumerated(EnumType.STRING)
    private TipoFalta tipo;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.data == null && this.criadoEm != null) {
            this.data = this.criadoEm.toLocalDate();
        }
    }
}