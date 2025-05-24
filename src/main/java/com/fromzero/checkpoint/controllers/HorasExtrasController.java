package com.fromzero.checkpoint.controllers;


import com.fromzero.checkpoint.dto.HorasExtrasAcumuladasDTO;
import com.fromzero.checkpoint.dto.HorasExtrasComparativoDTO;
import com.fromzero.checkpoint.dto.HorasExtrasDTO;
import com.fromzero.checkpoint.dto.HorasExtrasManualDTO;
import com.fromzero.checkpoint.dto.ResumoHorasExtrasMensalDTO;
import com.fromzero.checkpoint.entities.Folga;
import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.repositories.FolgaRepository;
import com.fromzero.checkpoint.repositories.HorasExtrasRepository;
import com.fromzero.checkpoint.services.HorasExtrasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/horas-extras")
public class HorasExtrasController {

    @Autowired
    private HorasExtrasService horasExtrasService;

    @Autowired
    private HorasExtrasRepository repository;

    @Autowired FolgaRepository folgaRepository;

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

    @PostMapping("/manual")
    public ResponseEntity<String> registrarHorasExtrasManual(@RequestBody HorasExtrasManualDTO dto) {
        horasExtrasService.registrarHorasExtrasManual(dto);
        return ResponseEntity.ok("Horas extras registradas com sucesso!");
    }

    @GetMapping("/colaborador/{colaboradorId}")
    public ResponseEntity<String> getTotalHorasExtrasPorColaborador(@PathVariable Long colaboradorId) {
        List<HorasExtras> horasExtras = repository.findByColaboradorId(colaboradorId);
        List<Folga> folgas = folgaRepository.findByColaboradorId(colaboradorId);
        int totalMinutos = 0;

        for (HorasExtras hora : horasExtras) {
            String saldoStr = hora.getSaldo().replace("h", "").trim();
            double horasDecimais = Double.parseDouble(saldoStr);
            int minutos = (int) Math.round(horasDecimais * 60);
            totalMinutos += minutos;
        }

        for (Folga folga : folgas) {
            String saldoStr = folga.getSaldoGasto().replace("h", "").trim();
            double horasDecimais = Double.parseDouble(saldoStr);
            int minutos = (int) Math.round(horasDecimais * 60);
            totalMinutos -= minutos;
        }

        if (totalMinutos < 0) totalMinutos = 0;

        int horasTotais = totalMinutos / 60;
        int minutosTotais = totalMinutos % 60;

        String resultado = String.format("%dh: %02dmin", horasTotais, minutosTotais);
        return ResponseEntity.ok(resultado);
    }
    
    @PostMapping
    public ResponseEntity<String> criarHorasExtras(@RequestBody HorasExtras novaHoraExtra) {
        novaHoraExtra.setCriadoEm(LocalDateTime.now());
        repository.save(novaHoraExtra);
        return ResponseEntity.ok("Horas extras criadas com sucesso!");
    }

    @GetMapping("/resumo-mensal")
    public ResponseEntity<ResumoHorasExtrasMensalDTO> getResumoMensalHorasExtras() {
        ResumoHorasExtrasMensalDTO resumo = horasExtrasService.calcularResumoMensalHorasExtras();
        return ResponseEntity.ok(resumo);
    }
}
