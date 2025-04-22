package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.dto.HorasExtrasAcumuladasDTO;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.entities.HorasExtras.Status;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.HorasExtrasRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorasExtrasService {

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;
    
    @Autowired
    private ColaboradorRepository colaboradorRepository;

    public List<HorasExtrasAcumuladasDTO> buscarHorasExtrasAcumuladasPorColaborador() {
        List<Colaborador> colaboradores = colaboradorRepository.findAll();
        
        return colaboradores.stream()
                .map(colaborador -> {
                    BigDecimal totalHoras = calcularTotalHorasExtrasAprovadas(colaborador.getId());
                    return new HorasExtrasAcumuladasDTO(
                            colaborador.getId(),
                            colaborador.getNome(),
                            totalHoras
                    );
                })
                .collect(Collectors.toList());
    }

    public List<HorasExtrasAcumuladasDTO> buscarHorasExtrasAcumuladasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<Colaborador> colaboradores = colaboradorRepository.findAll();
        
        return colaboradores.stream()
                .map(colaborador -> {
                    BigDecimal totalHoras = calcularTotalHorasExtrasAprovadasPorPeriodo(
                            colaborador.getId(), 
                            dataInicio, 
                            dataFim
                    );
                    return new HorasExtrasAcumuladasDTO(
                            colaborador.getId(),
                            colaborador.getNome(),
                            totalHoras
                    );
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calcularTotalHorasExtrasAprovadas(Long colaboradorId) {
        List<HorasExtras> horasExtras = horasExtrasRepository.findByColaboradorIdAndStatus(colaboradorId, Status.Aprovado);
        
        return horasExtras.stream()
                .map(h -> converterSaldoParaBigDecimal(h.getSaldo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTotalHorasExtrasAprovadasPorPeriodo(Long colaboradorId, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();
        
        List<HorasExtras> horasExtras = horasExtrasRepository.findByColaboradorIdAndStatusAndCriadoEmBetween(
                colaboradorId, 
                Status.Aprovado,
                inicio,
                fim
        );
        
        return horasExtras.stream()
                .map(h -> converterSaldoParaBigDecimal(h.getSaldo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal converterSaldoParaBigDecimal(String saldo) {
        try {
            return new BigDecimal(saldo.replace("h", "").replace(",", "."));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}