package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioFaltasService {

    private final FaltaRepository faltaRepository;
    private final ColaboradorRepository colaboradorRepository;

    public RelatorioFaltasService(FaltaRepository faltaRepository, ColaboradorRepository colaboradorRepository) {
        this.faltaRepository = faltaRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    public ByteArrayInputStream gerarRelatorio(Long colaboradorId, LocalDate dataInicio, LocalDate dataFim) throws Exception {
        List<Colaborador> colaboradores;

        if (colaboradorId != null) {
            Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));
            colaboradores = List.of(colaborador);
        } else {
            colaboradores = colaboradorRepository.findAll();
        }

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

        document.add(new Paragraph("Relatório de Faltas", tituloFont));
        document.add(new Paragraph("Período: " + dataInicio + " até " + dataFim));
        document.add(new Paragraph(" "));

        for (Colaborador colaborador : colaboradores) {
            document.add(new Paragraph("Colaborador: " + colaborador.getNome(), tituloFont));

            List<Falta> faltas = faltaRepository.findByColaboradorIdAndCriadoEmBetween(
                    colaborador.getId(),
                    dataInicio.atStartOfDay(),
                    dataFim.plusDays(1).atStartOfDay());

            if (faltas.isEmpty()) {
                document.add(new Paragraph("Sem faltas neste período.", normalFont));
            } else {
                for (Falta f : faltas) {
                    document.add(new Paragraph(
                            f.getCriadoEm().toLocalDate() + " - " +
                            f.getTipo() + " - " +
                            (f.getJustificado() ? "Justificada" : "Injustificada"), normalFont));
                }
            }

            document.add(new Paragraph(" "));
        }

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
