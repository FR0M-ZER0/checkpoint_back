package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "horas_extras") // Mantém do development (mais consistente com outras tabelas)
@Data // Adiciona Lombok da feat-holidays (sem quebrar funcionalidade)
public class HorasExtras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ext_id")
    private Long id; // Mantém tipo do development

    @Column(name = "ext_saldo", nullable = false)
    private String saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ext_status", nullable = false)
    private Status status; // Mantém enum do development

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador; // Mantém relacionamento JPA correto do development

    @Column(
        name = "criado_em", 
        updatable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP" // Combina ambas
    )
    private LocalDateTime criadoEm = LocalDateTime.now(); // Combina abordagens

    public enum Status {
        Aprovado,
        Rejeitado,
        Pendente
    }

    // Construtores manuais (removemos @AllArgsConstructor por segurança)
    public HorasExtras() {}
    
    public HorasExtras(String saldo, Status status, Colaborador colaborador) {
        this.saldo = saldo;
        this.status = status;
        this.colaborador = colaborador;
    }
}