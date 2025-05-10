package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="resposta")
@Data
public class Resposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="res_id")
    private Long id;

    @Column(name="res_mensagem")
    private String mensagem;

    @Column(name="res_lida")
    private Boolean lida = false;

    @Enumerated(EnumType.STRING)
    @Column(name="res_tipo")
    private TipoResposta tipo;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }

    public enum TipoResposta {
        folga, ferias, horas_extras, ponto, abono
    }
}
