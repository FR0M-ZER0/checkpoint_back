package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Resposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private String mensagem;

    @Column()
    private Boolean lida = false;

    @Enumerated(EnumType.STRING)
    @Column()
    private TipoResposta tipo;

    @ManyToOne
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @Column()
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }

    public enum TipoResposta {
        folga, ferias, horas_extras, ponto, abono
    }
}
