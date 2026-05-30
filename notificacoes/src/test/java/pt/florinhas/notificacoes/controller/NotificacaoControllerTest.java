package pt.florinhas.notificacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

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
                        utilizadorRepository);
    }

    @Test
    void listar_DeveRetornarLista() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        UserDetails user =
                User.withUsername("teste@test.com")
                        .password("123")
                        .authorities(List.of())
                        .build();

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(utilizador));

        when(notificacaoService.listarPorUtilizador(
                1L))
                .thenReturn(List.of());

        ResponseEntity<List<NotificacaoResponseDTO>> result =
                controller.listar(user);

        assertEquals(
                200,
                result.getStatusCode().value());
    }

    @Test
    void contarNaoLidas_DeveRetornarValor() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        UserDetails user =
                User.withUsername("teste@test.com")
                        .password("123")
                        .authorities(List.of())
                        .build();

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(utilizador));

        when(notificacaoService.contarNaoLidas(
                1L))
                .thenReturn(5L);

        ResponseEntity<Long> result =
                controller.contarNaoLidas(user);

        assertEquals(
                5L,
                result.getBody());
    }

    @Test
    void marcarComoLida_DeveExecutarService() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        UserDetails user =
                User.withUsername("teste@test.com")
                        .password("123")
                        .authorities(List.of())
                        .build();

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> result =
                controller.marcarComoLida(
                        2L,
                        user);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(notificacaoService)
                .marcarComoLida(
                        2L,
                        1L);
    }

    @Test
    void marcarTodasComoLidas_DeveExecutarService() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        UserDetails user =
                User.withUsername("teste@test.com")
                        .password("123")
                        .authorities(List.of())
                        .build();

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> result =
                controller.marcarTodasComoLidas(
                        user);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(notificacaoService)
                .marcarTodasComoLidas(1L);
    }

    @Test
    void eliminarNotificacao_DeveExecutarService() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        UserDetails user =
                User.withUsername("teste@test.com")
                        .password("123")
                        .authorities(List.of())
                        .build();

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> result =
                controller.eliminarNotificacao(
                        2L,
                        user);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(notificacaoService)
                .eliminarNotificacao(
                        2L,
                        1L);
    }

    @Test
    void eliminarTodas_DeveExecutarService() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        UserDetails user =
                User.withUsername("teste@test.com")
                        .password("123")
                        .authorities(List.of())
                        .build();

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(utilizador));

        ResponseEntity<Void> result =
                controller.eliminarTodas(user);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(notificacaoService)
                .eliminarTodas(1L);
    }
}