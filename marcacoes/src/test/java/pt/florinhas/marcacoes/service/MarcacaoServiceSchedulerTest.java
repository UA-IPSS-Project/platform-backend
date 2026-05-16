package pt.florinhas.marcacoes.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceSchedulerTest {

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @InjectMocks
    private MarcacaoService service;

    @Test
    void limparReservasExpiradas_DeveApagarReservas() {

        Marcacao m = new Marcacao();

        when(marcacaoRepository.findByEstadoAndCriadoEmBefore(
                eq(EventoEstado.EM_PREENCHIMENTO),
                any()
        )).thenReturn(List.of(m));

        service.limparReservasExpiradas();

        verify(marcacaoRepository).delete(m);
    }
    @Test
    void invalidarMarcacoesExpiradas_DeveAtualizarEstados() {

        when(marcacaoRepository.atualizarMarcacoesPorEstadoAntigas(
                any(), any(), any()
        )).thenReturn(2);

        when(marcacaoRepository.invalidarMarcacoesAntigas(
                any(), any(), any()
        )).thenReturn(3);

        service.invalidarMarcacoesExpiradas();

        verify(marcacaoRepository)
                .atualizarMarcacoesPorEstadoAntigas(any(), any(), any());

        verify(marcacaoRepository)
                .invalidarMarcacoesAntigas(any(), any(), any());
    }
}