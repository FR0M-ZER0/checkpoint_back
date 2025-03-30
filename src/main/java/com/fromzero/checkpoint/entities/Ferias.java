package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "ferias")
public class Ferias {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio; // Usando LocalDate ao invés de Date

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false)
    private Boolean aprovado = false;

    private String observacao;

    // Relacionamento muitos-para-um (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", insertable = false, updatable = false)
    private Colaborador colaborador;
}