package com.fromzero.checkpoint.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ferias")
public class Ferias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long colaboradorId;

    @Column(nullable = false)
    private Date dataInicio;

    @Column(nullable = false)
    private Date dataFim;

    @Column(nullable = false)
    private Boolean aprovado = false;

    private String observacao;


}
