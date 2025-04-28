package com.fromzero.checkpoint.services;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportService {

    public ByteArrayInputStream exportarHorasExtras(List<String[]> dados) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            // Cabeçalho do CSV
            writer.println("Nome,ID,Total de Horas");

            // Dados
            for (String[] linha : dados) {
                writer.println(String.join(",", linha));
            }

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao exportar horas extras: " + e.getMessage(), e);
        }
    }
}
