package com.fromzero.checkpoint.models;
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

    @Column(name = "ext_saldo", nullable = false)
    private String extSaldo;

    @Column(name = "ext_status", nullable = false)
    private String extStatus;

    @Column(name = "colaborador_id", nullable = false)
    private Integer colaboradorId;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", insertable = false, updatable = false)
    private Colaborador colaborador;

    // ... getters, setters, etc.
}