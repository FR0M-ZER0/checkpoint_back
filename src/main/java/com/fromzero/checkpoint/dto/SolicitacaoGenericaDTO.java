package com.fromzero.checkpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SolicitacaoGenericaDTO {
    private String tipo;
    private Object id;
    private String status;
    private LocalDateTime criadoEm;
    private String colaboradorNome;
}
