package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.repository.UtilizadorRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @Mock
    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(utilizadorRepository, cryptoUtils);
    }

    @Test
    @DisplayName("Email nulo deve lançar UsernameNotFoundException")
    void loadUserByUsername_EmailNulo_DeveLancarExcecao() {
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(null));
    }

    @Test
    @DisplayName("Email vazio deve lançar UsernameNotFoundException")
    void loadUserByUsername_EmailVazio_DeveLancarExcecao() {
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("   "));
    }

    @Test
    @DisplayName("Deve buscar por email com sucesso")
    void loadUserByUsername_DeveBuscarPorEmailComSucesso() {
        Utente utente = new Utente();
        utente.setEmail("teste@teste.com");

        when(utilizadorRepository.findByEmail("teste@teste.com")).thenReturn(List.of(utente));

        UserDetails result = service.loadUserByUsername("teste@teste.com");

        assertNotNull(result);
        assertEquals("teste@teste.com", result.getUsername());
        verify(utilizadorRepository).findByEmail("teste@teste.com");
    }

    @Test
    @DisplayName("Deve retornar o primeiro utilizador quando existem múltiplos com o mesmo email")
    void loadUserByUsername_MultiplosEmails_DeveRetornarPrimeiro() {
        Utente utente1 = new Utente();
        utente1.setEmail("teste@teste.com");
        Utente utente2 = new Utente();
        utente2.setEmail("teste@teste.com");

        when(utilizadorRepository.findByEmail("teste@teste.com")).thenReturn(List.of(utente1, utente2));

        UserDetails result = service.loadUserByUsername("teste@teste.com");

        assertNotNull(result);
        assertEquals(utente1, result);
    }

    @Test
    @DisplayName("Se email não existir, deve buscar e retornar por NIF com sucesso")
    void loadUserByUsername_DeveBuscarPorNifComSucesso() {
        Utente utente = new Utente();
        utente.setNif("123456789");

        when(utilizadorRepository.findByEmail("123456789")).thenReturn(List.of());
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("nif-hash-123");
        when(utilizadorRepository.findByNifHash("nif-hash-123")).thenReturn(List.of(utente));

        UserDetails result = service.loadUserByUsername("123456789");

        assertNotNull(result);
        verify(utilizadorRepository).findByEmail("123456789");
        verify(cryptoUtils).generateBlindIndex("123456789");
        verify(utilizadorRepository).findByNifHash("nif-hash-123");
    }

    @Test
    @DisplayName("Se email e NIF não existirem, deve lançar UsernameNotFoundException")
    void loadUserByUsername_EmailENifInexistentes_DeveLancarExcecao() {
        when(utilizadorRepository.findByEmail("invalido")).thenReturn(List.of());
        when(cryptoUtils.generateBlindIndex("invalido")).thenReturn("nif-hash-invalido");
        when(utilizadorRepository.findByNifHash("nif-hash-invalido")).thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("invalido"));
    }
}
