package pt.florinhas.requisicoes.security;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        cryptoUtils = mock(CryptoUtils.class);

        service = new CustomUserDetailsService(
                utilizadorRepository,
                cryptoUtils);
    }

    @Test
    void loadUserByUsername_DeveBuscarPorEmail() {

        Utilizador utilizador = mock(Utilizador.class);

        when(utilizadorRepository.findByEmail("teste@teste.com"))
                .thenReturn(List.of(utilizador));

        UserDetails result =
                service.loadUserByUsername("teste@teste.com");

        assertNotNull(result);
    }
    @Test
    void loadUserByUsername_DeveBuscarPorNif() {

        Utilizador utilizador = mock(Utilizador.class);

        when(utilizadorRepository.findByEmail("123"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(utilizador));

        UserDetails result =
                service.loadUserByUsername("123");

        assertNotNull(result);
    }

    @Test
    void loadUserByUsername_DeveLancarException() {

        when(utilizadorRepository.findByEmail(any()))
                .thenReturn(List.of());

        when(utilizadorRepository.findByNifHash(any()))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("abc"));
    }
}