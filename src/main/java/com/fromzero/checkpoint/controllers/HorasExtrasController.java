package com.fromzero.checkpoint.controllers;


import com.fromzero.checkpoint.dto.HorasExtrasAcumuladasDTO;
import com.fromzero.checkpoint.dto.HorasExtrasDTO;
import com.fromzero.checkpoint.dto.HorasExtrasManualDTO;
import com.fromzero.checkpoint.services.HorasExtrasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/horas-extras")
public class HorasExtrasController {

    @Autowired
    private HorasExtrasService horasExtrasService;


    // endpoint não está sendo usado
    /*

    @GetMapping("/{colaboradorId}")
    public ResponseEntity<List<HorasExtrasDTO>> getHorasExtrasPorMes(@PathVariable Long colaboradorId) {
        List<HorasExtrasDTO> saldoMensal = horasExtrasService.buscarSaldoMensalPorColaborador(colaboradorId);
        return ResponseEntity.ok(saldoMensal);
    }

    */

    @GetMapping("/acumuladas")
    public ResponseEntity<List<HorasExtrasAcumuladasDTO>> getHorasExtrasAcumuladas() {
        List<HorasExtrasAcumuladasDTO> horasAcumuladas = horasExtrasService.buscarHorasExtrasAcumuladasPorColaborador();
        return ResponseEntity.ok(horasAcumuladas);
    }

    @GetMapping("/acumuladas/periodo")
    public ResponseEntity<List<HorasExtrasAcumuladasDTO>> getHorasExtrasAcumuladasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        List<HorasExtrasAcumuladasDTO> horasAcumuladas = 
                horasExtrasService.buscarHorasExtrasAcumuladasPorPeriodo(inicio, fim);
        return ResponseEntity.ok(horasAcumuladas);
    }
}

    @PostMapping("/manual")
    public ResponseEntity<String> registrarHorasExtrasManual(@RequestBody HorasExtrasManualDTO dto) {
        horasExtrasService.registrarHorasExtrasManual(dto);
        return ResponseEntity.ok("Horas extras registradas com sucesso!");
    }
}
