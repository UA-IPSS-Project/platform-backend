package pt.florinhas.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

class CustomUserDetailsServiceTest {

    private UtilizadorRepository utilizadorRepository;
    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        utilizadorRepository = org.mockito.Mockito.mock(UtilizadorRepository.class);
        cryptoUtils = org.mockito.Mockito.mock(CryptoUtils.class);

        service = new CustomUserDetailsService(
                utilizadorRepository,
                cryptoUtils);
    }

    @Test
    void loadUserByUsername_DeveCarregarPorEmail() {
        Utente user = new Utente();
        user.setEmail("teste@teste.com");

        when(utilizadorRepository.findByEmail("teste@teste.com"))
                .thenReturn(List.of(user));

        UserDetails result =
                service.loadUserByUsername("teste@teste.com");

        assertEquals(user, result);

        verify(utilizadorRepository)
                .findByEmail("teste@teste.com");

        verify(utilizadorRepository, never())
                .findByNifHash(any());
    }

    @Test
    void loadUserByUsername_DeveCarregarPorNif() {
        Utente user = new Utente();

        when(utilizadorRepository.findByEmail("123456789"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        UserDetails result =
                service.loadUserByUsername("123456789");

        assertEquals(user, result);
    }

    @Test
    void loadUserByUsername_DeveLancarExcecaoQuandoNaoExiste() {
        when(utilizadorRepository.findByEmail("teste"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("teste"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("teste"));
    }

    @Test
    void loadUserByUsername_DeveLancarExcecaoQuandoUsernameNull() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null));
    }

    @Test
    void loadUserByUsername_DeveFazerTrim() {
        Utente user = new Utente();

        when(utilizadorRepository.findByEmail("teste@teste.com"))
                .thenReturn(List.of(user));

        service.loadUserByUsername("   teste@teste.com   ");

        verify(utilizadorRepository)
                .findByEmail("teste@teste.com");
    }

    @Test
    void loadUserByEmail_DeveRetornarUtilizador() {
        Utente user = new Utente();

        when(utilizadorRepository.findByEmail("email"))
                .thenReturn(List.of(user));

        UserDetails result = service.loadUserByEmail("email");

        assertEquals(user, result);
    }

    @Test
    void loadUserByEmail_DeveLancarExcecao() {
        when(utilizadorRepository.findByEmail("email"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByEmail("email"));
    }

    @Test
    void loadUserByNif_DeveRetornarUtilizador() {
        Utente user = new Utente();

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        UserDetails result = service.loadUserByNif("123");

        assertEquals(user, result);
    }

    @Test
    void loadUserByNif_DeveLancarExcecao() {
        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByNif("123"));
    }
}