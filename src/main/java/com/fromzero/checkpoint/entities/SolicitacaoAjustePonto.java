package com.fromzero.checkpoint.entities;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "solicitacao_ajuste_ponto")
@Data
public class SolicitacaoAjustePonto {
    @Id
    private String id;

    private String marcacaoId;

    public enum PeriodoMarcacao {
        inicio,
        pausa,
        retomada,
        saida
    }

    private PeriodoMarcacao periodo;

    public enum TipoMarcacao {
        edicao,
        exclusao,
        insercao
    }

    private TipoMarcacao tipo;

    public enum StatusMarcacao {
        pendente,
        rejeitado,
        aceito
    }

    private StatusMarcacao status;

    private String observacao;

    private LocalTime horario;

    private String colaboradorId;
    private String colaboradorNome;

    @CreatedDate
    private LocalDateTime criadoEm;
}
