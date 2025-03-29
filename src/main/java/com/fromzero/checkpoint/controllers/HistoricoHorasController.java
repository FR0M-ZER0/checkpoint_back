package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.dto.HistoricoHorasResponse;
import com.fromzero.checkpoint.services.HistoricoHorasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/historico")
public class HistoricoHorasController {

    @Autowired
    private HistoricoHorasService historicoHorasService;

    @GetMapping("/{ano}")
    public HistoricoHorasResponse getHistoricoPorAno(
            @PathVariable int ano,
            @RequestHeader("X-User-Id") Long colaboradorId) {
        return historicoHorasService.gerarHistoricoAnual(ano, colaboradorId);
    }
}