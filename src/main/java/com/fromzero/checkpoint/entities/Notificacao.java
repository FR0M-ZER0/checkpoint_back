package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="notificacao")
@Data
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="not_id")
    private Long id;

    @Column(name="not_mensagem")
    private String mensagem;

    @Column(name="not_lida")
    private Boolean lida;

    @Column(name="criado_em")
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador; 

    public enum NotificacaoTipo {
        folga,
        ferias,
        horasExtras,
        ponto,
        abono,
        falta
    }

    @Enumerated(EnumType.STRING)
    private NotificacaoTipo tipo;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
        if (this.lida == null) this.lida = false;
    }
}
