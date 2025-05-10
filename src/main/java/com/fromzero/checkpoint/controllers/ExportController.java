package com.fromzero.checkpoint.controllers;

import com.fromzero.checkpoint.services.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/horas-extras")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportarHorasExtras() {
        // Aqui ainda é mock, mas depois a gente puxa do banco.
        List<String[]> dados = Arrays.asList(
            new String[]{"Alice Silva", "1", "9h30m"},
            new String[]{"Bruno Lima", "2", "0h"},
            new String[]{"Carla Santos", "3", "5h"}
        );

        ByteArrayInputStream stream = exportService.exportarHorasExtras(dados);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=horas_extras.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(stream));
    }
}
