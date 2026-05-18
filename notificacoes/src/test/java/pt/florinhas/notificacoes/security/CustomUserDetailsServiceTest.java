package pt.florinhas.notificacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

import pt.florinhas.common_data.security.CustomUserDetailsService;

class CustomUserDetailsServiceTest {

    private UtilizadorRepository repository;

    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {

        repository =
                mock(UtilizadorRepository.class);

        cryptoUtils =
                mock(CryptoUtils.class);

        service =
                new CustomUserDetailsService(
                        repository,
                        cryptoUtils
                );
    }

    @Test
    void loadUserByUsername_DeveBuscarPorEmail() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail(
                "teste@teste.com"
        );

        when(repository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(utilizador));

        var result =
                service.loadUserByUsername(
                        "teste@teste.com"
                );

        assertEquals(
                utilizador,
                result
        );
    }

    @Test
    void loadUserByUsername_DeveBuscarPorNif() {

        Utilizador utilizador =
                new Utilizador();

        when(repository.findByEmail("123"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(repository.findByNifHash("hash"))
                .thenReturn(List.of(utilizador));

        var result =
                service.loadUserByUsername(
                        "123"
                );

        assertEquals(
                utilizador,
                result
        );
    }

    @Test
    void loadUserByUsername_DeveLancarErro() {

        when(repository.findByEmail("teste"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("teste"))
                .thenReturn("hash");

        when(repository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(
                        "teste"
                )
        );
    }

    @Test
    void loadUserByUsername_DeveLancarErroQuandoNull() {

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(
                        null
                )
        );
    }
}