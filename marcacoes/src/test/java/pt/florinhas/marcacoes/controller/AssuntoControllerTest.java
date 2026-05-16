package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.dto.AssuntoRequest;
import pt.florinhas.marcacoes.dto.AtualizarEstadoAssuntoRequest;
import pt.florinhas.marcacoes.service.AssuntoService;

class AssuntoControllerTest {

    @Mock
    private AssuntoService assuntoService;

    private AssuntoController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new AssuntoController(assuntoService);
    }

    private Assunto buildAssunto(Long id, String nome) {
        Assunto a = new Assunto();
        a.setId(id);
        a.setNome(nome);
        a.setAtivo(true);
        return a;
    }

    @Test
    void listarAtivos_DeveRetornarLista() {
        when(assuntoService.listarAtivos()).thenReturn(List.of(buildAssunto(1L, "Teste")));

        ResponseEntity<List<Assunto>> result = controller.listarAtivos();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void listarTodos_DeveRetornarLista() {
        when(assuntoService.listarTodos()).thenReturn(List.of(buildAssunto(1L, "Teste")));

        ResponseEntity<List<Assunto>> result = controller.listarTodos();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void criar_DeveRetornarCreated() {
        AssuntoRequest req = new AssuntoRequest("Novo");
        when(assuntoService.criar("Novo")).thenReturn(buildAssunto(1L, "Novo"));

        ResponseEntity<Assunto> result = controller.criar(req);

        assertEquals(201, result.getStatusCode().value());
        assertEquals("Novo", result.getBody().getNome());
    }

    @Test
    void atualizar_DeveRetornarOk() {
        AssuntoRequest req = new AssuntoRequest("Atualizado");
        when(assuntoService.atualizar(1L, "Atualizado")).thenReturn(buildAssunto(1L, "Atualizado"));

        ResponseEntity<Assunto> result = controller.atualizar(1L, req);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Atualizado", result.getBody().getNome());
    }

    @Test
    void apagar_DeveRetornarMensagem() {
        ResponseEntity<?> result = controller.apagar(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(assuntoService).apagar(1L);
    }

    @Test
    void atualizarEstado_DeveDelegarNoService() {
        AtualizarEstadoAssuntoRequest req = new AtualizarEstadoAssuntoRequest(true);
        when(assuntoService.setAtivo(1L, true)).thenReturn(buildAssunto(1L, "Teste"));

        ResponseEntity<Assunto> result = controller.atualizarEstado(1L, req);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isAtivo());
    }
}