package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

class CustomUserDetailsServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;
    @Mock
    private CryptoUtils cryptoUtils;

    private CustomUserDetailsService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CustomUserDetailsService(utilizadorRepository, cryptoUtils);
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
    @DisplayName("Deve falhar quando o username é nulo")
    void loadUserByUsername_DeveFalharQuandoUsernameNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null));
    }

    @Test
    @DisplayName("Deve buscar por email prioritariamente")
    void loadUserByUsername_DeveBuscarPorEmailPrimeiro() {
        Utilizador user = buildUser(1L, "user@test.com", "123456789");

        when(utilizadorRepository.findByEmail("user@test.com"))
                .thenReturn(List.of(user));

        UserDetails result = service.loadUserByUsername("user@test.com");

        assertEquals(user, result);
        verify(utilizadorRepository).findByEmail("user@test.com");
        verify(utilizadorRepository, never()).findByNifHash(anyString());
    }

    @Test
    @DisplayName("Deve fazer trim no email antes da busca")
    void loadUserByUsername_DeveFazerTrimNoEmail() {
        Utilizador user = buildUser(1L, "user@test.com", "123456789");

        when(utilizadorRepository.findByEmail("user@test.com"))
                .thenReturn(List.of(user));

        UserDetails result = service.loadUserByUsername("  user@test.com  ");

        assertEquals(user, result);
        verify(utilizadorRepository).findByEmail("user@test.com");
    }

    @Test
    @DisplayName("Deve buscar por NIF (Blind Index) quando não encontra por email")
    void loadUserByUsername_DeveBuscarPorNifQuandoNaoEncontraPorEmail() {
        Utilizador user = buildUser(1L, "user@test.com", "123456789");
        String nif = "123456789";
        String nifHash = "hashed_nif";

        when(utilizadorRepository.findByEmail(nif)).thenReturn(List.of());
        when(cryptoUtils.generateBlindIndex(nif)).thenReturn(nifHash);
        when(utilizadorRepository.findByNifHash(nifHash)).thenReturn(List.of(user));

        UserDetails result = service.loadUserByUsername(nif);

        assertEquals(user, result);
        verify(utilizadorRepository).findByEmail(nif);
        verify(cryptoUtils).generateBlindIndex(nif);
        verify(utilizadorRepository).findByNifHash(nifHash);
    }

    @Test
    @DisplayName("Deve falhar quando utilizador não existe em nenhum campo")
    void loadUserByUsername_DeveFalharQuandoNaoEncontraNemPorEmailNemPorNif() {
        String username = "unknown";
        String hash = "unknown_hash";

        when(utilizadorRepository.findByEmail(username)).thenReturn(List.of());
        when(cryptoUtils.generateBlindIndex(username)).thenReturn(hash);
        when(utilizadorRepository.findByNifHash(hash)).thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(username));
    }
}