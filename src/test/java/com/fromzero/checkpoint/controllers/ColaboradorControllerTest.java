package com.fromzero.checkpoint.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

@WebMvcTest(ColaboradorController.class)
public class ColaboradorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColaboradorRepository colaboradorRepository;

    @MockitoBean
    private FaltaRepository faltaRepository;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCadastrarColaborador() throws Exception {
        Colaborador colaborador = new Colaborador();
        colaborador.setId(1L);
        colaborador.setNome("João");
        colaborador.setEmail("joao@email.com");

        when(colaboradorRepository.save(any(Colaborador.class))).thenReturn(colaborador);

        mockMvc.perform(post("/colaborador")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(colaborador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    public void testObterColaboradores() throws Exception {
        Colaborador c1 = new Colaborador();
        c1.setId(1L);
        c1.setNome("Alice");

        Colaborador c2 = new Colaborador();
        c2.setId(2L);
        c2.setNome("Bob");

        when(colaboradorRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/colaborador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Alice"))
                .andExpect(jsonPath("$[1].nome").value("Bob"));
    }

    @Test
    public void testObterColaboradorPorId() throws Exception {
        Colaborador colaborador = new Colaborador();
        colaborador.setId(1L);
        colaborador.setNome("Carlos");

        when(colaboradorRepository.findById(1L)).thenReturn(Optional.of(colaborador));

        mockMvc.perform(get("/colaborador/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Carlos"));
    }

    @Test
    public void testObterFaltasPorColaborador() throws Exception {
        Falta falta1 = new Falta();
        falta1.setId(1L);

        Falta falta2 = new Falta();
        falta2.setId(2L);

        when(faltaRepository.findByColaboradorId(1L)).thenReturn(Arrays.asList(falta1, falta2));

        mockMvc.perform(get("/colaborador/faltas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    public void testObterFaltasSemSolicitacao() throws Exception {
        Falta falta = new Falta();
        falta.setId(1L);

        when(faltaRepository.obterFaltasSemSolicitacao(1L)).thenReturn(Arrays.asList(falta));

        mockMvc.perform(get("/colaborador/falta/sem-solicitacao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void testObterNotificacoesNaoLidas() throws Exception {
        Notificacao notificacao = new Notificacao();
        notificacao.setId(1L);
        notificacao.setMensagem("Nova notificação");

        when(notificacaoService.buscarNotificacoesNaoLidas(1L)).thenReturn(Arrays.asList(notificacao));

        mockMvc.perform(get("/colaborador/notificacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mensagem").value("Nova notificação"));
    }

    @Test
    public void testErroObterFaltasColaboradorSemFaltas() throws Exception {
        when(faltaRepository.findByColaboradorId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/colaborador/faltas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testErroObterFaltasSemSolicitacaoColaboradorSemFaltas() throws Exception {
        when(faltaRepository.obterFaltasSemSolicitacao(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/colaborador/falta/sem-solicitacao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testErroObterNotificacoesNaoLidasSemNotificacoes() throws Exception {
        when(notificacaoService.buscarNotificacoesNaoLidas(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/colaborador/notificacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
