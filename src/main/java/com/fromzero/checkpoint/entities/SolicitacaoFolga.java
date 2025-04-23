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
    private Long solFolId;

    @Column(name = "sol_fol_data", nullable = false)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate solFolData;

    @Column(name = "sol_fol_observacao")
    private String solFolObservacao;
    
    public enum Status {
        Pendente,
        Rejeitado,
        Aceito
    }
    @Column(name = "sol_fol_status", nullable = false)
    private Status solFolStatus;

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @Column(name = "sol_fol_saldo_gasto", nullable = false)
    private String solFolSaldoGasto;
    
    @Column(name = "sol_fol_tipo")
    private String solFolTipo;
    
    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }

}