package pt.florinhas.notificacoes.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

class CustomUserDetailsServiceTest {

    private UtilizadorRepository utilizadorRepository;
    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {

        utilizadorRepository =
                mock(UtilizadorRepository.class);

        cryptoUtils =
                mock(CryptoUtils.class);

        service =
                new CustomUserDetailsService(
                        utilizadorRepository,
                        cryptoUtils);
    }

    @Test
    void loadUserByUsername_DeveRetornarPorEmail() {

        Utilizador user =
                new Utilizador();

        user.setEmail("teste@test.com");

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(user));

        UserDetails result =
                service.loadUserByUsername(
                        "teste@test.com");

        assertEquals(
                "teste@test.com",
                result.getUsername());
    }

    @Test
    void loadUserByUsername_DeveRetornarPorNif() {

        Utilizador user =
                new Utilizador();

        user.setEmail("teste@test.com");

        when(utilizadorRepository.findByEmail(
                "123"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash(
                "hash"))
                .thenReturn(List.of(user));

        UserDetails result =
                service.loadUserByUsername("123");

        assertEquals(
                "teste@test.com",
                result.getUsername());
    }

    @Test
    void loadUserByUsername_DeveLancarErroQuandoNull() {

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(
                        null));
    }

    @Test
    void loadUserByUsername_DeveLancarErroQuandoNaoExiste() {

        when(utilizadorRepository.findByEmail(
                "teste"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex(
                "teste"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash(
                "hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(
                        "teste"));
    }

    @Test
    void loadUserByEmail_DeveRetornarUser() {

        Utilizador user =
                new Utilizador();

        user.setEmail("teste@test.com");

        when(utilizadorRepository.findByEmail(
                "teste@test.com"))
                .thenReturn(List.of(user));

        UserDetails result =
                service.loadUserByEmail(
                        "teste@test.com");

        assertEquals(
                "teste@test.com",
                result.getUsername());
    }

    @Test
    void loadUserByNif_DeveRetornarUser() {

        Utilizador user =
                new Utilizador();

        user.setEmail("teste@test.com");

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash(
                "hash"))
                .thenReturn(List.of(user));

        UserDetails result =
                service.loadUserByNif("123");

        assertEquals(
                "teste@test.com",
                result.getUsername());
    }
}