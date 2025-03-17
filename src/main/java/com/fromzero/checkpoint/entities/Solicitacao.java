package com.fromzero.checkpoint.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Solicitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String mensagem;
    
    public enum SolicitacaoStatus {
        Aprovado,
        Rejeitado,
        Pendente
    }

    @Enumerated(EnumType.STRING)
    private SolicitacaoStatus status;

    @ManyToOne(cascade = CascadeType.ALL)
    private Colaborador colaborador;
}
