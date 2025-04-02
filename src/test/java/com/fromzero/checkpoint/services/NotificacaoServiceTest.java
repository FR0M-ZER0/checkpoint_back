package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Notificacao;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.NotificacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificacaoServiceTest {  // Adicionei 'public' aqui

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private Colaborador alice;
    private Notificacao notificacao;

    @BeforeEach
    public void setUp() {  // Adicionei 'public' aqui
        alice = new Colaborador();
        alice.setId(1L);
        alice.setNome("Alice Silva");
        alice.setEmail("alice@gmail.com");
        alice.setSenhaHash("senha123");
        alice.setAtivo(true);

        notificacao = new Notificacao();
        notificacao.setId(1L);
        notificacao.setMensagem("Teste de notificação");
        notificacao.setTipo(NotificacaoTipo.ponto);
        notificacao.setColaborador(alice);
        notificacao.setLida(false);
        notificacao.setCriadoEm(LocalDateTime.now());
    }

    @Test
    public void testCriarNotificacao() {  // Adicionei 'public' aqui
        when(notificacaoRepository.save(any(Notificacao.class))).thenReturn(notificacao);

        Notificacao resultado = notificacaoService.criaNotificacao(
            "Lembrete de ponto", 
            NotificacaoTipo.ponto, 
            alice
        );

        assertNotNull(resultado);
        assertEquals("Alice Silva", resultado.getColaborador().getNome());
        verify(messagingTemplate).convertAndSendToUser(
            eq("1"), 
            eq("/queue/notificacoes"), 
            any(Notificacao.class)
        );
    }

    @Test
    public void testBuscarNotificacoesNaoLidas() {  // Adicionei 'public' aqui
        when(notificacaoRepository.findByColaboradorIdAndLidaFalse(1L))
            .thenReturn(List.of(notificacao));

        List<Notificacao> resultado = notificacaoService.buscarNotificacoesNaoLidas(1L);

        assertEquals(1, resultado.size());
        assertEquals("Alice Silva", resultado.get(0).getColaborador().getNome());
    }

    @Test
    public void testEnviarNotificacaoHorasExtras() {  // Adicionei 'public' aqui
        when(notificacaoRepository.save(any(Notificacao.class))).thenReturn(notificacao);

        notificacaoService.enviarNotificacaoHorasExtras(
            alice, 
            LocalDateTime.of(2025, 4, 1, 18, 0)
        );

        verify(notificacaoRepository).save(argThat(notif -> 
            notif.getMensagem().contains("18:00") &&
            notif.getColaborador().getId().equals(1L)
        ));
    }

    @Test
    public void testMarcarComoLida() {  // Adicionei 'public' aqui
        when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao));
        when(notificacaoRepository.save(any(Notificacao.class))).thenReturn(notificacao);

        Notificacao resultado = notificacaoService.marcarComoLida(1L);

        assertTrue(resultado.getLida());
    }
}