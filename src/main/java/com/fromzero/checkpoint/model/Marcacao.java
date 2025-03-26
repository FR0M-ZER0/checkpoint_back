package com.fromzero.checkpoint.model;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.persistence.Id;

@Document(collection = "marcacoes") // coleção de marcações
public class Marcacao {

    @Id
    private String id; // id da marcação

    @NotNull(message = "ID do colaborador é obrigatório")
    private Long colaboradorId; // id do colaborador

    @NotNull(message = "Data e hora da marcação são obrigatórias")
    private LocalDateTime dataHora; // data e hora da marcação

    @NotNull(message = "Tipo da marcação é obrigatório")
    private TipoMarcacao tipo; // tipo da marcação ("ENTRADA", "PAUSA", "RETOMADA" ou "SAIDA")

    @NotNull(message = "Campo obrigatório")
    private Boolean processada; // indica se a marcação já foi processada

    // Construtor padrão
    public Marcacao() {}

    // Construtor com parâmetros
    public Marcacao(Long colaboradorId, LocalDateTime dataHora, TipoMarcacao tipo, Boolean processada) {
        this.colaboradorId = colaboradorId;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.processada = processada;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public TipoMarcacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMarcacao tipo) {
        this.tipo = tipo;
    }

    public Boolean isProcessada() {
        return processada;
    }

    public void setProcessada(Boolean processada) {
        this.processada = processada;
    }

    // Enum para TipoMarcacao
    public enum TipoMarcacao {
        ENTRADA("ENTRADA"),
        PAUSA("PAUSA"),
        RETOMADA("RETOMADA"),
        SAIDA("SAIDA");

        private final String value;

        TipoMarcacao(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TipoMarcacao fromValue(String value) {
            for (TipoMarcacao tipo : TipoMarcacao.values()) {
                if (tipo.getValue().equals(value)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Invalid value for TipoMarcacao: " + value);
        }
    }
}
    