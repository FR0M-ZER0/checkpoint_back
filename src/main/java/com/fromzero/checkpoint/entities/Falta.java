package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "falta")
@Data
public class Falta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fal_id")
    private Long id;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    public enum TipoFalta {
        Atraso,
        Ausencia
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "fal_tipo")
    private TipoFalta tipo;

    @Column(name = "fal_justificado")
    private Boolean justificado;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }
    
    @OneToOne(mappedBy = "falta", cascade = CascadeType.ALL)
    @JsonIgnore
    private SolicitacaoAbonoFalta solicitacaoAbonoFalta;
}
