package com.fromzero.checkpoint.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "horas_extras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorasExtras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ext_id")
    private Integer extId;

    @Column(name = "ext_saldo")
    private String saldo;

    @Column(name = "ext_status", nullable = false)
    private String extStatus;

    @Column(name = "colaborador_id")
    private Long colaboradorId;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    // ... getters, setters, etc.
}