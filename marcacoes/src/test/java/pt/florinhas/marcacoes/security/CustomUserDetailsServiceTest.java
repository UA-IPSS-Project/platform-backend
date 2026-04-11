package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.repository.UtilizadorRepository;

import pt.florinhas.common_data.domain.Utente;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setEmail("test@example.com");
        utente.setNome("Test User");
        utente.setPassHash("hashedPassword");
    }

    @Test
    void loadUserByUsername_DeveRetornarUserDetails_QuandoUtilizadorExiste() {
        // Arrange
        when(utilizadorRepository.findByEmail("test@example.com"))
            .thenReturn(List.of(utente));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        verify(utilizadorRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_DeveLancarException_QuandoUtilizadorNaoExiste() {
        // Arrange
        when(utilizadorRepository.findByEmail(anyString()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent@example.com"));

        assertTrue(exception.getMessage().contains("Utilizador não encontrado"));
        verify(utilizadorRepository).findByEmail("nonexistent@example.com");
    }
}
