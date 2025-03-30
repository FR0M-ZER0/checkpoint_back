package com.fromzero.checkpoint.entities;


import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;

import jakarta.persistence.*;

import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "colaborador")
@Data
public class Colaborador {
    @Id
    @Column()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "col_nome")
    private String nome;

    @Column(name = "col_email", unique = true, nullable = false)
    private String email;

    @Column(name = "col_senha")
    private String senhaHash;

    @Column(name = "col_ativo")
    private Boolean ativo = true;


    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Resposta> respostas;


    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "saldo_ferias", columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private Double saldoFerias = 0.00;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
        if (this.saldoFerias == null) this.saldoFerias = 0.00;
    }
}