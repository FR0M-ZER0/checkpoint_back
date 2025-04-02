package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "jornadas")
public class Jornada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String escala;

    @Column
    private String cargaHoraria;

    @Column(name = "inicio_previsto")
    private LocalDateTime inicioPrevisto;

    @Column(name = "data")
    private LocalDateTime data;

    @Column
    private LocalDateTime criadaEm;

    @ManyToOne
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @OneToMany(mappedBy = "jornada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargaDiaria> cargasDiarias;
    
    @OneToMany(mappedBy = "jornada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargaFlexivel> cargasFlexiveis;

    @PrePersist
    public void prePersist() {
        if (this.criadaEm == null) {
            this.criadaEm = LocalDateTime.now();
        }
        if (this.data == null) {
            this.data = LocalDateTime.now();
        }
    }
}