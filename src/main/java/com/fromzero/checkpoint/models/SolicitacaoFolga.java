package com.fromzero.checkpoint.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_folga")
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
    private Integer colaboradorId;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @Column(name = "sol_fol_saldo_gasto", nullable = false)
    private String solFolSaldoGasto; // Adicione este campo

    // ... getters, setters, etc.
}
