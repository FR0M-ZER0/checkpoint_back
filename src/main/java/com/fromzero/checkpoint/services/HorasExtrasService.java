package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.dto.HorasExtrasDTO;
import com.fromzero.checkpoint.dto.HorasExtrasManualDTO;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Gestor;
import com.fromzero.checkpoint.entities.HorasExtras;
import com.fromzero.checkpoint.entities.HorasExtrasManual;
import com.fromzero.checkpoint.entities.HorasExtras.Status;
import com.fromzero.checkpoint.entities.HorasExtrasManual.Tipo;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.GestorRepository;
import com.fromzero.checkpoint.repositories.HorasExtrasManualRepository;
import com.fromzero.checkpoint.repositories.HorasExtrasRepository;

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

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private GestorRepository gestorRepository;

    @Autowired
    private HorasExtrasManualRepository horasExtrasManualRepository;

    public List<HorasExtrasDTO> buscarSaldoMensalPorColaborador(Long colaboradorId) {
        LocalDateTime agora = LocalDateTime.now();
        int anoAtual = agora.getYear();
        LocalDateTime julhoInicio = LocalDateTime.of(anoAtual, Month.JULY, 1, 0, 0);

        List<HorasExtras> todasHoras = horasExtrasRepository.findByColaboradorId(colaboradorId);

        List<HorasExtras> aprovadas = todasHoras.stream()
                .filter(h -> h.getStatus() == Status.Aprovado)
                .toList();

        boolean existePosJulho = aprovadas.stream()
                .anyMatch(h -> h.getCriadoEm().isAfter(julhoInicio) || h.getCriadoEm().isEqual(julhoInicio));

        List<HorasExtras> filtradas = aprovadas.stream()
                .filter(h -> {
                    if (existePosJulho) {
                        return h.getCriadoEm().isAfter(julhoInicio) || h.getCriadoEm().isEqual(julhoInicio);
                    } else {
                        return h.getCriadoEm().isBefore(julhoInicio);
                    }
                })
                .toList();

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

    public void registrarHorasExtrasManual(HorasExtrasManualDTO dto) {
        Colaborador colaborador = colaboradorRepository.findById(dto.getColaboradorId())
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

        Gestor gestor = gestorRepository.findById(dto.getGestorId())
                .orElseThrow(() -> new RuntimeException("Gestor não encontrado"));

        // Salvar a hora extra somente se for tipo 'adicao' ou 'edicao'
        HorasExtras horas = null;
        if (dto.getTipo().equalsIgnoreCase("adicao") || dto.getTipo().equalsIgnoreCase("edicao")) {
            horas = new HorasExtras();
            horas.setColaborador(colaborador);
            horas.setSaldo(dto.getSaldo() + "h");
            horas.setStatus(Status.valueOf(capitalize(dto.getStatus().toLowerCase())));
            horas.setCriadoEm(LocalDateTime.now());
            horas = horasExtrasRepository.save(horas);
        }

        // Criar registro no log de alterações
        HorasExtrasManual manual = new HorasExtrasManual();
        manual.setGestor(gestor);
        manual.setHorasExtras(horas); // pode ser null se for exclusão
        manual.setTipo(Tipo.valueOf(dto.getTipo().toLowerCase())); // adicao, edicao ou exclusao
        manual.setCriadoEm(LocalDateTime.now());

        horasExtrasManualRepository.save(manual);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
