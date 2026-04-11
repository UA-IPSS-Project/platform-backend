package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;
import pt.florinhas.marcacoes.service.NotificacaoService;
import pt.florinhas.marcacoes.service.UtilizadorService;

class NotificacaoControllerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private UtilizadorService utilizadorService;

    @Mock
    private UserDetails userDetails;

    private NotificacaoController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new NotificacaoController(notificacaoService, utilizadorService);
    }

    private Utilizador buildUser() {
        Utilizador u = new Utente();
        u.setId(1L);
        u.setEmail("user@test.com");
        u.setNome("User");
        return u;
    }

    @Test
    void listar_DeveRetornarNotificacoesDoUtilizador() {
        Utilizador user = buildUser();

        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(utilizadorService.buscarPorEmail("user@test.com")).thenReturn(user);
        when(notificacaoService.listarPorUtilizador(1L)).thenReturn(List.of());

        ResponseEntity<List<NotificacaoResponseDTO>> result = controller.listar(userDetails);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        verify(notificacaoService).listarPorUtilizador(1L);
    }

    @Test
    void contarNaoLidas_DeveRetornarCount() {
        Utilizador user = buildUser();

        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(utilizadorService.buscarPorEmail("user@test.com")).thenReturn(user);
        when(notificacaoService.contarNaoLidas(1L)).thenReturn(3L);

        ResponseEntity<Long> result = controller.contarNaoLidas(userDetails);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(3L, result.getBody());
    }

    @Test
    void marcarComoLida_DeveDelegarNoService() {
        Utilizador user = buildUser();

        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(utilizadorService.buscarPorEmail("user@test.com")).thenReturn(user);

        ResponseEntity<Void> result = controller.marcarComoLida(10L, userDetails);

        assertEquals(200, result.getStatusCode().value());
        verify(notificacaoService).marcarComoLida(10L, 1L);
    }

    @Test
    void marcarTodasComoLidas_DeveDelegarNoService() {
        Utilizador user = buildUser();

        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(utilizadorService.buscarPorEmail("user@test.com")).thenReturn(user);

        ResponseEntity<Void> result = controller.marcarTodasComoLidas(userDetails);

        assertEquals(200, result.getStatusCode().value());
        verify(notificacaoService).marcarTodasComoLidas(1L);
    }

    @Test
    void eliminarNotificacao_DeveDelegarNoService() {
        Utilizador user = buildUser();

        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(utilizadorService.buscarPorEmail("user@test.com")).thenReturn(user);

        ResponseEntity<Void> result = controller.eliminarNotificacao(11L, userDetails);

        assertEquals(200, result.getStatusCode().value());
        verify(notificacaoService).eliminarNotificacao(11L, 1L);
    }

    @Test
    void eliminarTodas_DeveDelegarNoService() {
        Utilizador user = buildUser();

        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(utilizadorService.buscarPorEmail("user@test.com")).thenReturn(user);

        ResponseEntity<Void> result = controller.eliminarTodas(userDetails);

        assertEquals(200, result.getStatusCode().value());
        verify(notificacaoService).eliminarTodas(1L);
    }
}