package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.dto.AssuntoRequest;
import pt.florinhas.marcacoes.dto.AtualizarEstadoAssuntoRequest;
import pt.florinhas.marcacoes.service.AssuntoService;

class AssuntoControllerTest {

    private AssuntoService service;
    private AssuntoController controller;

    @BeforeEach
    void setUp() {

        service =
                mock(AssuntoService.class);

        controller =
                new AssuntoController(service);
    }

    @Test
    void listarAtivos_DeveRetornarLista() {

        when(service.listarAtivos())
                .thenReturn(List.of());

        ResponseEntity<List<Assunto>> result =
                controller.listarAtivos();

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void criar_DeveRetornar201() {

        Assunto assunto =
                new Assunto("Teste");

        when(service.criar("Teste"))
                .thenReturn(assunto);

        ResponseEntity<Assunto> result =
                controller.criar(
                        new AssuntoRequest("Teste"));

        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    void apagar_DeveInvocarService() {

        controller.apagar(1L);

        verify(service)
                .apagar(1L);
    }

    @Test
    void atualizarEstado_DeveRetornarAssunto() {

        Assunto assunto =
                new Assunto("Teste");

        when(service.setAtivo(1L, true))
                .thenReturn(assunto);

        ResponseEntity<Assunto> result =
                controller.atualizarEstado(
                        1L,
                        new AtualizarEstadoAssuntoRequest(true));

        assertEquals(200, result.getStatusCode().value());
    }
}