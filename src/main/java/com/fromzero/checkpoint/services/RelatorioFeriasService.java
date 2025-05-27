package com.fromzero.checkpoint.services;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioFeriasService {

    private final FeriasRepository feriasRepository;
    private final ColaboradorRepository colaboradorRepository;

    public RelatorioFeriasService(FeriasRepository feriasRepository, ColaboradorRepository colaboradorRepository) {
        this.feriasRepository = feriasRepository;
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

        document.add(new Paragraph("Relatório de Férias", tituloFont));
        document.add(new Paragraph("Período: " + dataInicio + " até " + dataFim));
        document.add(new Paragraph(" "));

        for (Colaborador colaborador : colaboradores) {
            document.add(new Paragraph("Colaborador: " + colaborador.getNome(), tituloFont));

            List<Ferias> ferias = feriasRepository.findByColaboradorIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                    colaborador.getId(),
                    dataFim, // Use dataFim for dataInicioLessThanEqual
                    dataInicio // Use dataInicio for dataFimGreaterThanEqual
            );

            if (ferias.isEmpty()) {
                document.add(new Paragraph("Sem férias neste período.", normalFont));
            } else {
                for (Ferias m : ferias) {
                    document.add(new Paragraph(
                            m.getDataInicio() + " - " + 
                            m.getDataFim()));
                }
            }

            document.add(new Paragraph(" "));
        }

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
