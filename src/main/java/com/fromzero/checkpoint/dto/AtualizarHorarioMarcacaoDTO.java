package com.fromzero.checkpoint.dto;

import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarHorarioMarcacaoDTO {
    @NotNull(message = "Novo horário é obrigatório")
    private LocalTime novoHorario;
}
