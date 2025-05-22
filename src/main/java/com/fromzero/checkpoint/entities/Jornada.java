package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "jornada") // Use o nome exato da sua tabela
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Ou Integer, se você decidiu manter INT no banco para esta tabela

    @Column(name = "escala", nullable = false) // Ex: "6x1", "5x2", "4x3"
    private String escala;

    @Column(name = "carga_horaria", nullable = false) // Ex: "40" (para 40h semanais)
    private String cargaHoraria; 

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_jornada", nullable = false)
    private TipoJornada tipoJornada;

    // inicio_previsto pode ser null para jornadas flexíveis
    @Column(name = "inicio_previsto", nullable = true) 
    private LocalTime inicioPrevisto;

    @Column(name = "criado_em", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    // Os campos "data" e "colaborador_id" que você mencionou antes para a tabela "jornada"
    // foram omitidos aqui, pois geralmente uma definição de jornada não é atrelada
    // a uma data específica ou a um colaborador específico. Se eles têm outro propósito, me avise.

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.tipoJornada == null) { // Garante um valor padrão
            this.tipoJornada = TipoJornada.FLEXIVEL;
        }
    }
}