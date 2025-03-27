package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.Marcacao;
import com.fromzero.checkpoint.entities.Marcacao.TipoMarcacao;
import com.fromzero.checkpoint.repositories.MarcacaoLogRepository;
import com.fromzero.checkpoint.repositories.MarcacaoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceTest {

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @InjectMocks
    private MarcacaoService marcacaoService;

    private Marcacao marcacao;

    @Mock
    private MarcacaoLogRepository marcacaoLogRepository;

    @BeforeEach
    void setUp() {
        marcacao = new Marcacao();
        marcacao.setId("123");
        marcacao.setColaboradorId(1L);
        marcacao.setDataHora(LocalDateTime.now());
        marcacao.setTipo(TipoMarcacao.ENTRADA);
        marcacao.setProcessada(false);
    }

    @Test
    void deveCriarMarcacaoComSucesso() {
        System.out.println("Iniciando teste deveCriarMarcacaoComSucesso...");
    
        when(marcacaoRepository.findByColaboradorIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of()); // Nenhuma marcação existente no mesmo dia
        
        when(marcacaoRepository.save(any(Marcacao.class))).thenReturn(marcacao);
    
        Marcacao novaMarcacao = marcacaoService.criarMarcacao(marcacao);
    
        System.out.println("Marcacao criada: " + novaMarcacao);
    
        assertNotNull(novaMarcacao);
        assertEquals(marcacao.getId(), novaMarcacao.getId());
        verify(marcacaoRepository, times(1)).save(any(Marcacao.class));
    
        System.out.println("Teste deveCriarMarcacaoComSucesso finalizado com sucesso.");
    }    

    @Test
    void deveLancarErroQuandoMarcacaoDuplicada() {
        when(marcacaoRepository.findByColaboradorIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of(marcacao)); // Já existe uma marcação do mesmo tipo

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marcacaoService.criarMarcacao(marcacao);
        });

        assertEquals("Já existe uma marcação do mesmo tipo para este colaborador hoje.", exception.getMessage());
        verify(marcacaoRepository, never()).save(any(Marcacao.class));
    }

    @Test
    void deveBuscarMarcacaoPorId() {
        when(marcacaoRepository.findById("123")).thenReturn(Optional.of(marcacao));

        Optional<Marcacao> resultado = marcacaoService.buscarMarcacaoPorId("123");

        assertTrue(resultado.isPresent());
        assertEquals(marcacao.getId(), resultado.get().getId());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemMarcacoes() {
        when(marcacaoRepository.findAll()).thenReturn(List.of());

        List<Marcacao> marcacoes = marcacaoService.listarMarcacoes();

        assertTrue(marcacoes.isEmpty());
    }

    @Test
    void deveExcluirMarcacaoNaoProcessada() {
        when(marcacaoRepository.findById("123")).thenReturn(Optional.of(marcacao));

        marcacaoService.deletarMarcacao("123");

        verify(marcacaoRepository, times(1)).deleteById("123");
    }

    @Test
    void deveLancarErroAoExcluirMarcacaoProcessada() {
        marcacao.setProcessada(true);
        when(marcacaoRepository.findById("123")).thenReturn(Optional.of(marcacao));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marcacaoService.deletarMarcacao("123");
        });

        assertEquals("Não é possível deletar uma marcação processada", exception.getMessage());
        verify(marcacaoRepository, never()).deleteById("123");
    }
}