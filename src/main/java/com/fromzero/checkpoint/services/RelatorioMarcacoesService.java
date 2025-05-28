package com.fromzero.checkpoint.services;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioMarcacoesService {

    private final MarcacaoRepository marcacaoRepository;
    private final ColaboradorRepository colaboradorRepository;

    public RelatorioMarcacoesService(MarcacaoRepository marcacaoRepository, ColaboradorRepository colaboradorRepository) {
        this.marcacaoRepository = marcacaoRepository;
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

        document.add(new Paragraph("Relatório de Marcações", tituloFont));
        document.add(new Paragraph("Período: " + dataInicio + " até " + dataFim));
        document.add(new Paragraph(" "));

        for (Colaborador colaborador : colaboradores) {
            document.add(new Paragraph("Colaborador: " + colaborador.getNome(), tituloFont));

            List<Marcacao> marcacoes = marcacaoRepository.findByColaboradorIdAndDataHoraBetween(
                    colaborador.getId(), 
                    dataInicio.atStartOfDay(),
                    dataFim.plusDays(1).atStartOfDay());

            if (marcacoes.isEmpty()) {
                document.add(new Paragraph("Sem marcações neste período.", normalFont));
            } else {
                for (Marcacao m : marcacoes) {
                    document.add(new Paragraph(
                            m.getDataHora().toLocalDate() + " - " + 
                            m.getDataHora().toLocalTime() + " - " + 
                            m.getTipo(), normalFont));
                }
            }

            document.add(new Paragraph(" "));
        }

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
