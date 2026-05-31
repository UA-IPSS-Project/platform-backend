package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;

class AuthorizationServiceTest {

    private UtilizadorRepository repository;

    private AuthorizationService service;

    @BeforeEach
    void setUp() {

        repository = mock(UtilizadorRepository.class);

        service = new AuthorizationService(repository);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_DeveRetornarIdDoUtilizador() {

        Utilizador utilizador = new Utilizador();

        utilizador.setId(1L);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        utilizador,
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        Long result = service.getCurrentUserId();

        assertEquals(1L, result);
    }

    @Test
    void getCurrentUserId_DeveUsarUserDetails() {

        User user =
                new User(
                        "teste@test.com",
                        "pass",
                        List.of());

        Utilizador utilizador = new Utilizador();

        utilizador.setId(5L);

        when(repository.findByEmail("teste@test.com"))
                .thenReturn(List.of(utilizador));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        Long result = service.getCurrentUserId();

        assertEquals(5L, result);
    }

    @Test
    void hasAnyRole_DeveRetornarTrue() {

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "user",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_SECRETARIA")));

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        assertEquals(true,
                service.hasAnyRole("ROLE_SECRETARIA"));
    }

    @Test
    void isAdmin_DeveRetornarTrue() {

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "user",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_SECRETARIA")));

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        assertEquals(true, service.isAdmin());
    }

    @Test
    void checkPermission_DevePermitirMesmoUtilizador() {

        Utilizador utilizador = new Utilizador();

        utilizador.setId(1L);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        utilizador,
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        service.checkPermission(1L, "recurso");
    }

    @Test
    void checkPermission_DeveLancarErro() {

        Utilizador utilizador = new Utilizador();

        utilizador.setId(1L);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        utilizador,
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        assertThrows(
                AccessDeniedException.class,
                () -> service.checkPermission(2L, "recurso"));
    }
}