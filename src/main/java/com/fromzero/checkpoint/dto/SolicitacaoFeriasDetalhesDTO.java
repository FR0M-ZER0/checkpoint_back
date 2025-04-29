package com.fromzero.checkpoint.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.fromzero.checkpoint.entities.SolicitacaoFerias; // Importe a entidade

@Data
@NoArgsConstructor
public class SolicitacaoFeriasDetalhesDTO {
    private Long id;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String observacaoColaborador;
    private String comentarioGestor;
    private String status;
    private String nomeColaborador; 

    // Construtor para mapear da entidade
    public SolicitacaoFeriasDetalhesDTO(SolicitacaoFerias sf) {
        this.id = sf.getId();
        this.dataInicio = sf.getDataInicio();
        this.dataFim = sf.getDataFim();
        this.observacaoColaborador = sf.getObservacao();
        this.comentarioGestor = sf.getComentarioGestor();
        this.status = sf.getStatus();
        if (sf.getColaborador() != null) { // Garante que colaborador foi carregado
            this.nomeColaborador = sf.getColaborador().getNome();
        } else {
             this.nomeColaborador = "Colab. ID: " + sf.getColaboradorId();
        }
    }
}