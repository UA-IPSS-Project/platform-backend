package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;

class CustomUserDetailsServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CustomUserDetailsService(utilizadorRepository);
    }

    private Utilizador buildUser(Long id, String email, String nif) {
        Utilizador u = new Utente();
        u.setId(id);
        u.setEmail(email);
        u.setNif(nif);
        u.setNome("User");
        return u;
    }

    @Test
    void loadUserByUsername_DeveFalharQuandoUsernameNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null));
    }

    @Test
    void loadUserByUsername_DeveBuscarPorEmailPrimeiro() {
        Utilizador user = buildUser(1L, "user@test.com", "100000002");

        when(utilizadorRepository.findByEmail("user@test.com"))
                .thenReturn(List.of(user));

        var result = service.loadUserByUsername("user@test.com");

        assertSame(user, result);
        verify(utilizadorRepository).findByEmail("user@test.com");
        verify(utilizadorRepository, never()).findByNif(anyString());
    }

    @Test
    void loadUserByUsername_DeveFazerTrimNoEmail() {
        Utilizador user = buildUser(1L, "user@test.com", "100000002");

        when(utilizadorRepository.findByEmail("user@test.com"))
                .thenReturn(List.of(user));

        var result = service.loadUserByUsername("  user@test.com  ");

        assertSame(user, result);
        verify(utilizadorRepository).findByEmail("user@test.com");
    }

    @Test
    void loadUserByUsername_DeveBuscarPorNifQuandoNaoEncontraPorEmail() {
        Utilizador user = buildUser(1L, "user@test.com", "100000002");

        when(utilizadorRepository.findByEmail("100000002"))
                .thenReturn(List.of());
        when(utilizadorRepository.findByNif("100000002"))
                .thenReturn(List.of(user));

        var result = service.loadUserByUsername("100000002");

        assertSame(user, result);
        verify(utilizadorRepository).findByEmail("100000002");
        verify(utilizadorRepository).findByNif("100000002");
    }

    @Test
    void loadUserByUsername_DeveFalharQuandoNaoEncontraNemPorEmailNemPorNif() {
        when(utilizadorRepository.findByEmail("x@test.com"))
                .thenReturn(List.of());
        when(utilizadorRepository.findByNif("x@test.com"))
                .thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("x@test.com"));
    }

    @Test
    void loadUserByEmail_DeveRetornarUtilizador() {
        Utilizador user = buildUser(1L, "user@test.com", "100000002");

        when(utilizadorRepository.findByEmail("user@test.com"))
                .thenReturn(List.of(user));

        var result = service.loadUserByEmail("user@test.com");

        assertSame(user, result);
    }

    @Test
    void loadUserByEmail_DeveFalharQuandoNaoExiste() {
        when(utilizadorRepository.findByEmail("user@test.com"))
                .thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByEmail("user@test.com"));
    }

    @Test
    void loadUserByNif_DeveRetornarUtilizador() {
        Utilizador user = buildUser(1L, "user@test.com", "100000002");

        when(utilizadorRepository.findByNif("100000002"))
                .thenReturn(List.of(user));

        var result = service.loadUserByNif("100000002");

        assertSame(user, result);
    }

    @Test
    void loadUserByNif_DeveFalharQuandoNaoExiste() {
        when(utilizadorRepository.findByNif("100000002"))
                .thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByNif("100000002"));
    }
}