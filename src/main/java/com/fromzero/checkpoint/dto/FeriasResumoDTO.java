package com.fromzero.checkpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class FeriasResumoDTO {
    private String nomeColaborador;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private long diasTotais;
    private String status;
}
