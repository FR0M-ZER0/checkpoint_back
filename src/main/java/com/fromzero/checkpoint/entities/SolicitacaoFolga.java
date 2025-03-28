package com.fromzero.checkpoint.entities;

import java.time.LocalDate;
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
import lombok.Data;

@Entity
@Data
public class SolicitacaoFolga {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sol_fol_id")
	private Long solFolId;
	
	@Column(name = "sol_fol_data")
	private LocalDate solFolData;
	
	@Column(name = "sol_fol_observacao")
	private String solFolObservacao;
	
	public enum SolicitacaoStatus {
        Aprovado,
        Rejeitado,
        Pendente
    }
	
	@Enumerated(EnumType.STRING)
	@Column(name = "sol_fol_status")
    private SolicitacaoStatus status;
	
	@ManyToOne
    @JoinColumn(nullable = false, name = "colaborador_id")
    private Colaborador colaborador;
	
	@Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
    }
}
