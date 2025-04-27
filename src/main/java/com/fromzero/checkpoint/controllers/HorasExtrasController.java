package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.dto.HorasExtrasDTO;
import com.fromzero.checkpoint.dto.HorasExtrasManualDTO;
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

    @GetMapping("/{colaboradorId}")
    public ResponseEntity<List<HorasExtrasDTO>> getHorasExtrasPorMes(@PathVariable Long colaboradorId) {
        List<HorasExtrasDTO> saldoMensal = horasExtrasService.buscarSaldoMensalPorColaborador(colaboradorId);
        return ResponseEntity.ok(saldoMensal);
    }

    @PostMapping("/manual")
    public ResponseEntity<String> registrarHorasExtrasManual(@RequestBody HorasExtrasManualDTO dto) {
        horasExtrasService.registrarHorasExtrasManual(dto);
        return ResponseEntity.ok("Horas extras registradas com sucesso!");
    }
}
