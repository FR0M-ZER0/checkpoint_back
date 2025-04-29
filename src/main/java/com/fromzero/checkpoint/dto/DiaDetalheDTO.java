package com.fromzero.checkpoint.dto; // Ajuste o pacote

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiaDetalheDTO {
    private String statusDia; // Ex: "FÉRIAS", "FOLGA", "FALTA", "NORMAL"
    private SolicitacaoFeriasDetalhesDTO detalhesFerias; // Preenchido se statusDia for "FÉRIAS"
    // Adicionar outros DTOs de detalhes aqui se necessário (ex: detalhesFolga, detalhesFalta)
    // private FolgaDetalhesDTO detalhesFolga; 
    // private FaltaDetalhesDTO detalhesFalta;
}