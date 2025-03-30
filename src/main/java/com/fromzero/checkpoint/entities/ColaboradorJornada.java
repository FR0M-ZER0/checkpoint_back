package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "colaborador_jornada")
public class ColaboradorJornada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jornada_id")
    private Long jornadaId;

    @Column(name = "colaborador_id")
    private Long colaboradorId;
}