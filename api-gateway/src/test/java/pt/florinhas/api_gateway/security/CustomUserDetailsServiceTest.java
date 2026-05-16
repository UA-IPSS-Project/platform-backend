package pt.florinhas.api_gateway.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @Mock
    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(
                utilizadorRepository,
                cryptoUtils);
    }

    @Test
    void loadUserByUsername_DeveBuscarPorEmail() {

        Utente utente = new Utente();
        utente.setEmail("teste@teste.com");

        when(utilizadorRepository.findByEmail("teste@teste.com"))
                .thenReturn(List.of(utente));

        UserDetails result =
                service.loadUserByUsername("teste@teste.com");

        assertNotNull(result);
        verify(utilizadorRepository)
                .findByEmail("teste@teste.com");
    }

    @Test
    void loadUserByUsername_DeveBuscarPorNif() {

        Utente utente = new Utente();
        utente.setNif("123456789");

        when(utilizadorRepository.findByEmail("123456789"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(utente));

        UserDetails result =
                service.loadUserByUsername("123456789");

        assertNotNull(result);
    }
    @Test
    void loadUserByUsername_DeveLancarExcecao() {

        when(utilizadorRepository.findByEmail("teste"))
                .thenReturn(List.of());

        when(cryptoUtils.generateBlindIndex("teste"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("teste")
        );
    }
    @Test
        void loadUserByUsername_DeveRetornarPrimeiroUserQuandoExistemMultiplos() {

        Utente utente1 =
                new Utente();

        utente1.setEmail(
                "teste@teste.com"
        );

        Utente utente2 =
                new Utente();

        utente2.setEmail(
                "teste2@teste.com"
        );

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(
                        utente1,
                        utente2
                ));

        UserDetails result =
                service.loadUserByUsername(
                        "teste@teste.com"
                );

        assertNotNull(result);

        assertEquals(
                utente1,
                result
        );
        }
}