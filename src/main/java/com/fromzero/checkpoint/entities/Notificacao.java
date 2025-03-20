package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String mensagem;

    @Column
    private Boolean lida;

    @Column
    private LocalDateTime criadoEm;

    @ManyToOne
    private Colaborador colaborador;

    public enum NotificacaoTipo {
        folga,
        ferias,
        horasExtras,
        ponto,
        abono
    }

    @Enumerated(EnumType.STRING)
    private NotificacaoTipo tipo;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
        if (this.lida == null) this.lida = false;
    }
}
