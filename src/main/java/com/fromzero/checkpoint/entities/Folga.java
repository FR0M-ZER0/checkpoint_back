package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Folga")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Folga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "colaborador_id", nullable = false)
    private Integer colaboradorId;

    @Column(name = "saldo_gasto", nullable = false)
    private String saldoGasto;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", insertable = false, updatable = false)
    private Colaborador colaborador;


}