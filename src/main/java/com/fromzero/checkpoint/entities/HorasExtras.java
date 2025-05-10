package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "horas_extras")
public class HorasExtras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ext_id")
    private Long id;

    @Column(name = "ext_saldo", nullable = false)
    private String saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ext_status", nullable = false)
    private Status status;

    @Column(name = "colaborador_id", nullable = false)
    private Long colaboradorId;

    @Column(name = "justificativa", nullable = false, length = 500)
    private String justificativa;

    @Column(
        name = "criado_em",     
        updatable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP" // Combina ambas
    )
    private LocalDateTime criadoEm = LocalDateTime.now(); // Combina abordagens

    public enum Status {
        Aprovado, Rejeitado, Pendente
    }

    // Construtores
    public HorasExtras() {}
    
    public HorasExtras(String saldo, Status status, Long colaboradorId) {
        this.saldo = saldo;
        this.status = status;
        this.colaboradorId = colaboradorId;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSaldo() {
        return saldo;
    }

    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}