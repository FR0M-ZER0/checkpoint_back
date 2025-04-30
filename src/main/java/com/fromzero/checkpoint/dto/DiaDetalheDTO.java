package com.fromzero.checkpoint.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiaDetalheDTO {
    private String statusDia; // Ex: "FÉRIAS", "FOLGA", "FALTA", "NORMAL"
    private SolicitacaoFeriasDetalhesDTO detalhesFerias; // Preenchido se statusDia for "FÉRIAS"
    // private FolgaDetalhesDTO detalhesFolga; // Adicionar depois para Folga
    // private FaltaDetalhesDTO detalhesFalta; // Adicionar depois para Falta
    // ... outros detalhes ...
}