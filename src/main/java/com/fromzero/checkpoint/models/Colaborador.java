package com.fromzero.checkpoint.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "colaborador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Colaborador_id")
    private Integer colaboradorId;

    @Column(name = "Nome", nullable = false)
    private String nome;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "Ativo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo;

    @Column(name = "Criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @Column(name = "saldo_ferias", columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private Double saldoFerias;
}
