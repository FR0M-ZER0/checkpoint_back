package com.fromzero.checkpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolicitacaoResumoDTO {
    private long totalPendentes;
    private long criadasHoje;
    private long criadasOntem;
    private long diferencaHojeOntem;
}
