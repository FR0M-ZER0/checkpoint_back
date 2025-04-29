package com.fromzero.checkpoint.dto; // Ajuste o pacote

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SolicitacaoFeriasDetalhesDTO {
    private Long id;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String observacaoColaborador; // Observação de quem pediu
    private String comentarioGestor; // Comentário de quem aprovou/rejeitou
    private String status;
    private String nomeColaborador; // Nome para exibição

    // Construtor para facilitar a criação a partir da entidade
    public SolicitacaoFeriasDetalhesDTO(com.fromzero.checkpoint.entities.SolicitacaoFerias sf) {
        this.id = sf.getId();
        this.dataInicio = sf.getDataInicio();
        this.dataFim = sf.getDataFim();
        this.observacaoColaborador = sf.getObservacao();
        this.comentarioGestor = sf.getComentarioGestor();
        this.status = sf.getStatus();
        if (sf.getColaborador() != null) { // Garante que colaborador foi carregado (JOIN FETCH)
            this.nomeColaborador = sf.getColaborador().getNome();
        } else {
             this.nomeColaborador = "ID: " + sf.getColaboradorId(); // Fallback
        }
    }
}