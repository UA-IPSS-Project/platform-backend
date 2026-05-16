package pt.florinhas.notificacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.notificacoes.service.NotificacaoService;

class NotificacaoControllerTest {

    private NotificacaoService notificacaoService;

    private UtilizadorRepository utilizadorRepository;

    private NotificacaoController controller;

    @BeforeEach
    void setUp() {

        notificacaoService =
                mock(NotificacaoService.class);

        utilizadorRepository =
                mock(UtilizadorRepository.class);

        controller =
                new NotificacaoController(
                        notificacaoService,
                        utilizadorRepository
                );
    }

    private User createUserDetails() {

        return new User(
                "teste@teste.com",
                "123",
                List.of()
        );
    }

    private Utilizador createUtilizador() {

        Utilizador u =
                new Utilizador();

        u.setId(1L);
        u.setEmail("teste@teste.com");

        return u;
    }

    @Test
    void listar_DeveRetornarLista() {

        User user =
                createUserDetails();

        Utilizador utilizador =
                createUtilizador();

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        when(notificacaoService.listarPorUtilizador(1L))
                .thenReturn(List.of(
                        new NotificacaoResponseDTO()
                ));

        ResponseEntity<List<NotificacaoResponseDTO>> response =
                controller.listar(user);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertEquals(
                1,
                response.getBody().size()
        );
    }

    @Test
    void contarNaoLidas_DeveRetornarValor() {

        User user =
                createUserDetails();

        Utilizador utilizador =
                createUtilizador();

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        when(notificacaoService.contarNaoLidas(1L))
                .thenReturn(5L);

        ResponseEntity<Long> response =
                controller.contarNaoLidas(user);

        assertEquals(
                5L,
                response.getBody()
        );
    }

    @Test
    void marcarComoLida_DeveExecutar() {

        User user =
                createUserDetails();

        Utilizador utilizador =
                createUtilizador();

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> response =
                controller.marcarComoLida(
                        10L,
                        user
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(notificacaoService)
                .marcarComoLida(
                        10L,
                        1L
                );
    }

    @Test
    void marcarTodasComoLidas_DeveExecutar() {

        User user =
                createUserDetails();

        Utilizador utilizador =
                createUtilizador();

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> response =
                controller.marcarTodasComoLidas(user);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(notificacaoService)
                .marcarTodasComoLidas(1L);
    }

    @Test
    void eliminarNotificacao_DeveExecutar() {

        User user =
                createUserDetails();

        Utilizador utilizador =
                createUtilizador();

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> response =
                controller.eliminarNotificacao(
                        15L,
                        user
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(notificacaoService)
                .eliminarNotificacao(
                        15L,
                        1L
                );
    }

    @Test
    void eliminarTodas_DeveExecutar() {

        User user =
                createUserDetails();

        Utilizador utilizador =
                createUtilizador();

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> response =
                controller.eliminarTodas(user);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(notificacaoService)
                .eliminarTodas(1L);
    }
}