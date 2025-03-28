package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.dto.HorasExtrasDTO;
import com.fromzero.checkpoint.services.HorasExtrasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horas-extras")
public class HorasExtrasController {

    @Autowired
    private HorasExtrasService horasExtrasService;

    // GET /horas-extras/{colaboradorId}
    @GetMapping("/{colaboradorId}")
    public ResponseEntity<List<HorasExtrasDTO>> getHorasExtrasPorMes(@PathVariable Long colaboradorId) {
        List<HorasExtrasDTO> saldoMensal = horasExtrasService.buscarSaldoMensalPorColaborador(colaboradorId);
        return ResponseEntity.ok(saldoMensal);
    }
}
