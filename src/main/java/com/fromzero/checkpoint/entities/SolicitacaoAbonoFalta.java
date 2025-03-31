package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "solicitacao_abono_falta")
@Data
public class SolicitacaoAbonoFalta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sol_abo_id")
    private Long id;
    
    @Column(name = "sol_abo_motivo")
    private String motivo;
    
    public enum SolicitacaoStatus {
        Aprovado,
        Rejeitado,
        Pendente
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "sol_abo_status")
    private SolicitacaoStatus status;

    @Column(name = "sol_abo_justificativa")
    private String justificativa;

    @Column(name = "sol_abo_arquivo_caminho")
    private String arquivoCaminho;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "falta_id", nullable = false)
    private Falta falta;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }
}
