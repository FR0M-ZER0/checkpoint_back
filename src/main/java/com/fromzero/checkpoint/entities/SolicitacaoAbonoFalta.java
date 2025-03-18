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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class SolicitacaoAbonoFalta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String motivo;
    
    public enum SolicitacaoStatus {
        Aprovado,
        Rejeitado,
        Pendente
    }

    @Enumerated(EnumType.STRING)
    private SolicitacaoStatus status;

    @Column
    private String justificativa;

    @Column
    private String arquivoCaminho;

    @OneToOne(cascade = CascadeType.ALL)
    private Falta falta;

    @Column
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }
}
