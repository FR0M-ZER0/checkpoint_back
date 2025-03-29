package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_abono_ferias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoAbonoFerias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // Alterado para Long

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;  // Alterado para Long

    @Column(name = "dias_vendidos", nullable = false)
    private Integer diasVendidos;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDate dataSolicitacao;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    // Relacionamento muitos-para-um (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", insertable = false, updatable = false)
    private Colaborador colaborador;
}