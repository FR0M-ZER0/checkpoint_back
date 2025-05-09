package com.fromzero.checkpoint.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.SolicitacaoAbonoFalta;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAbonoFaltaRepository;
import com.fromzero.checkpoint.services.FileUploadService;
import com.fromzero.checkpoint.services.NotificacaoService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class AbonoFaltaController {
    @Autowired
    private SolicitacaoAbonoFaltaRepository repository;

    @Autowired
    private FaltaRepository faltaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private FileUploadService fileUploadService;

    @Value("${upload.dir}")
    private String uploadDir;

    @PostMapping("/abonar-falta")
    public SolicitacaoAbonoFalta cadastrarAbonoFalta(
            @RequestParam("motivo") String motivo,
            @RequestParam("faltaId") Long faltaId,
            @RequestParam("justificativa") String justificativa,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo) {

        Falta falta = faltaRepository.findById(faltaId)
                .orElseThrow(() -> new RuntimeException("Falta não existe"));

        SolicitacaoAbonoFalta solicitacao = new SolicitacaoAbonoFalta();
        solicitacao.setMotivo(motivo);
        solicitacao.setFalta(falta);
        solicitacao.setJustificativa(justificativa);
        solicitacao.setStatus(SolicitacaoAbonoFalta.SolicitacaoStatus.Pendente);

        if (arquivo != null && !arquivo.isEmpty()) {
            String arquivoCaminho = fileUploadService.saveFile(arquivo);
            solicitacao.setArquivoCaminho(arquivoCaminho);
        }

        repository.save(solicitacao);

        notificacaoService.criaNotificacao("Sua solicitação de abono de falta foi solicitada", NotificacaoTipo.abono, falta.getColaborador());
        
        return solicitacao;
    }  

    @GetMapping("/files/{filename}")
        public ResponseEntity<Resource> getFile(@PathVariable String filename) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
                Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Arquivo não encontrado: " + filename);
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar arquivo: " + e.getMessage());
        }
    }
}
