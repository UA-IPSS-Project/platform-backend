package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

class CustomUserDetailsServiceTest {

    private UtilizadorRepository repository;
    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {

        repository = mock(UtilizadorRepository.class);

        cryptoUtils = mock(CryptoUtils.class);

        service = new CustomUserDetailsService(repository, cryptoUtils);
    }

    @Test
    void loadUserByUsername_DeveCarregarPorEmail() {

        Utente user =
                new Utente();

        when(repository.findByEmail("teste"))
                .thenReturn(List.of(user));

        assertEquals(
                user,
                service.loadUserByUsername("teste"));
    }

    @Test
    void loadUserByUsername_DeveCarregarPorNif() {

        Utente user =
                new Utente();

        when(repository.findByEmail("123"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(repository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        assertEquals(
                user,
                service.loadUserByUsername("123"));
    }

    @Test
    void loadUserByUsername_DeveFalhar() {

        when(repository.findByEmail("teste"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("teste"))
                .thenReturn("hash");

        when(repository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("teste"));
    }
}