package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "Solicitacao_folga")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoFolga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sol_fol_id")
    private Integer solFolId;

    @Column(name = "sol_fol_data", nullable = false)
    private LocalDate solFolData;

    @Column(name = "sol_fol_observacao")
    private String solFolObservacao;

    @Column(name = "sol_fol_status", nullable = false)
    private String solFolStatus;

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @Column(name = "sol_fol_saldo_gasto", nullable = false)
    private String solFolSaldoGasto;

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "colaborador_id", referencedColumnName = "col_id", insertable = false, updatable = false)
    private Colaborador colaborador;
}