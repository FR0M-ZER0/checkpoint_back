package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.entities.HorasExtras.Status;
import com.fromzero.checkpoint.repositories.HorasExtrasRepository;
import com.fromzero.checkpoint.dto.HorasExtrasDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class HorasExtrasService {

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;

    public List<HorasExtrasDTO> buscarSaldoMensalPorColaborador(Long colaboradorId) {
        LocalDateTime agora = LocalDateTime.now();
        int anoAtual = agora.getYear();
        LocalDateTime julhoInicio = LocalDateTime.of(anoAtual, Month.JULY, 1, 0, 0);
    
        // Primeiro busca todas as horas extras aprovadas do colaborador
        List<HorasExtras> todasHoras = horasExtrasRepository.findByColaboradorId(colaboradorId);
    
        // Filtra por status Aprovado
        List<HorasExtras> aprovadas = todasHoras.stream()
                .filter(h -> h.getStatus() == Status.Aprovado)
                .toList();
    
        // Verifica se existe alguma aprovada a partir de julho
        boolean existePosJulho = aprovadas.stream()
                .anyMatch(h -> h.getCriadoEm().isAfter(julhoInicio) || h.getCriadoEm().isEqual(julhoInicio));
    
        // Filtra conforme a lógica
        List<HorasExtras> filtradas = aprovadas.stream()
                .filter(h -> {
                    if (existePosJulho) {
                        return h.getCriadoEm().isAfter(julhoInicio) || h.getCriadoEm().isEqual(julhoInicio);
                    } else {
                        return h.getCriadoEm().isBefore(julhoInicio);
                    }
                })
                .toList();
    
        // Agrupa por mês e ano e soma as horas
        return filtradas.stream()
                .collect(Collectors.groupingBy(h -> h.getCriadoEm().getMonth()))
                .entrySet().stream()
                .map(entry -> {
                    BigDecimal totalHoras = entry.getValue().stream()
                            .map(h -> {
                                try {
                                    return new BigDecimal(h.getSaldo().replace("h", "").replace(",", "."));
                                } catch (Exception e) {
                                    return BigDecimal.ZERO;
                                }
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
                    String mesNome = entry.getKey().getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
                    String mesAno = mesNome + "/" + anoAtual;
    
                    return new HorasExtrasDTO(mesAno, totalHoras);
                })
                .collect(Collectors.toList());
    }    
}
