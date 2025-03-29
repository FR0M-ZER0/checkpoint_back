package com.fromzero.checkpoint.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.SolicitacaoAbonoFaltaRepository;
import com.fromzero.checkpoint.services.FileUploadService;
import com.fromzero.checkpoint.services.NotificacaoService;

@WebMvcTest(AbonoFaltaController.class)
public class AbonoFaltaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitacaoAbonoFaltaRepository repository;

    @MockitoBean
    private FaltaRepository faltaRepository;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @MockitoBean
    private FileUploadService fileUploadService;

    @Test
    public void testCadastrarAbonoFalta_Sucesso() throws Exception {
        Falta falta = new Falta();
        falta.setId(1L);

        when(faltaRepository.findById(1L)).thenReturn(java.util.Optional.of(falta));

        mockMvc.perform(post("/abonar-falta")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("motivo", "doença")
                .param("faltaId", "1")
                .param("justificativa", "Teste de justificativa"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCadastrarAbonoFalta_FaltaNaoExiste() throws Exception {
        when(faltaRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/abonar-falta")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("motivo", "doença")
                .param("faltaId", "1")
                .param("justificativa", "Teste de justificativa"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetFile_Sucesso() throws Exception {
        String filename = "teste.pdf";
        Path filePath = Paths.get("uploads").resolve(filename).normalize();
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);

        mockMvc.perform(get("/files/" + filename))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetFile_ArquivoNaoExiste() throws Exception {
        mockMvc.perform(get("/files/inexistente.pdf"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testCadastrarAbonoFalta_ArquivoObrigatorioParaAlgunsMotivos() throws Exception {
        Falta falta = new Falta();
        falta.setId(1L);

        when(faltaRepository.findById(1L)).thenReturn(java.util.Optional.of(falta));

        mockMvc.perform(post("/abonar-falta")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("motivo", "doença")
                .param("faltaId", "1")
                .param("justificativa", "Estou doente"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCadastrarAbonoFalta_ComArquivo() throws Exception {
        Falta falta = new Falta();
        falta.setId(1L);

        when(faltaRepository.findById(1L)).thenReturn(java.util.Optional.of(falta));
        when(fileUploadService.saveFile(any(MultipartFile.class))).thenReturn("uploads/teste.pdf");

        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "teste.pdf", "application/pdf", "conteudo".getBytes());

        mockMvc.perform(multipart("/abonar-falta")
                .file(file)
                .param("motivo", "doença")
                .param("faltaId", "1")
                .param("justificativa", "Estou doente"))
                .andExpect(status().isOk());

        verify(fileUploadService, times(1)).saveFile(any(MultipartFile.class));
    }

    @Test
    public void testCriacaoDeNotificacao() throws Exception {
        Falta falta = new Falta();
        falta.setId(1L);
        Colaborador colaborador = new Colaborador();
        falta.setColaborador(colaborador);

        when(faltaRepository.findById(1L)).thenReturn(java.util.Optional.of(falta));

        mockMvc.perform(post("/abonar-falta")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("motivo", "doença")
                .param("faltaId", "1")
                .param("justificativa", "Estou doente"))
                .andExpect(status().isOk());

        verify(notificacaoService, times(1))
                .criaNotificacao(eq("Sua solicitação de abono de falta foi solicitada"),
                                 eq(Notificacao.NotificacaoTipo.abono),
                                 eq(colaborador));
    }
}
