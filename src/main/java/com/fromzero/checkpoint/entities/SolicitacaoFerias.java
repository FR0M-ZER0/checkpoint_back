package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "solicitacao_ferias") // Mapeia para a tabela correta
public class SolicitacaoFerias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sol_fer_id")
    private Long id;

    @Column(name = "sol_fer_data_inicio", nullable = false)
    private LocalDate dataInicio; // Pode chamar só dataInicio no Java

    @Column(name = "sol_fer_data_fim", nullable = false)
    private LocalDate dataFim; // Pode chamar só dataFim no Java

    @Column(name = "sol_fer_observacao")
    private String observacao; // Mapeia para sol_fer_observacao

    @Column(name = "sol_fer_status", nullable = false)
    private String status; // Mapeia para sol_fer_status (String é mais simples que Enum aqui)

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", referencedColumnName = "col_id", insertable = false, updatable = false)
    private Colaborador colaborador;

    // Se quiser mapear criado_em:
    // @Column(name = "criado_em", insertable = false, updatable = false)
    // private java.sql.Timestamp criadoEm;
}