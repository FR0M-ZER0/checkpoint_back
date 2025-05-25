package com.fromzero.checkpoint.dto;

import com.fromzero.checkpoint.entities.TipoJornada;
import java.time.LocalTime;

public record JornadaAtivaDTO(
    String escala,
    TipoJornada tipoJornada,
    String cargaHoraria,
    LocalTime inicioPrevisto
) {}