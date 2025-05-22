package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "colaborador_jornada") // Nome exato da sua tabela
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Ou Integer

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "jornada_id", nullable = false)
    private Jornada jornada; // A definição da jornada

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId; // O ID do colaborador. Ajuste para Integer se seu Colaborador.id for Integer

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio; // Quando esta jornada se aplica

    @Column(name = "data_fim") // Pode ser nulo se for a jornada atual sem data para terminar
    private LocalDate dataFim;
}