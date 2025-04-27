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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;


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

        notificarColaboradorPorEmail(colaborador, dto.getSaldo(), dto.getTipo());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    
    @Autowired
    private JavaMailSender mailSender;

    private void notificarColaboradorPorEmail(Colaborador colaborador, String saldo, String tipo) {
        if (colaborador.getEmail() != null && !colaborador.getEmail().isEmpty()) {
            String acao;
            switch (tipo.toLowerCase()) {
                case "adicao":
                    acao = "lançadas";
                    break;
                case "edicao":
                    acao = "atualizadas";
                    break;
                case "exclusao":
                    acao = "removidas";
                    break;
                default:
                    acao = "processadas"; // Caso venha algo errado
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(colaborador.getEmail());
            message.setSubject("Atualização de Horas Extras");
            message.setText("Olá " + colaborador.getNome() + ",\n\n" +
                            "As suas horas extras foram " + acao + " com sucesso.\n" +
                            "Quantidade: " + saldo + "h\n\n" +
                            "Se tiver qualquer dúvida, entre em contato com seu gestor.\n\n" +
                            "Atenciosamente,\n" +
                            "Equipe CheckPoint");

            mailSender.send(message);
        }
    }
}

