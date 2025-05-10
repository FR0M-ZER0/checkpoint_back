package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;  // Faltava importar o jakarta.persistence.*
import lombok.Data;             // E também o lombok.Data

import java.time.LocalDateTime;

@Entity
@Table(name = "gestor_altera_horas_extras")
@Data
public class HorasExtrasManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gestor_id", nullable = false)
    private Gestor gestor;

    @ManyToOne
    @JoinColumn(name = "horas_extras_id", nullable = false)
    private HorasExtras horasExtras;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(name = "criado_em", updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    public enum Tipo {
        adicao, edicao, exclusao
    }

    @Column(name = "justificativa", nullable = false, length = 500) // <-- Aqui adicionamos a justificativa
    private String justificativa;

}
