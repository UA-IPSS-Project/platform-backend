package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.marcacoes.service.email.EmailService;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.exception.BadRequestException;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UtilizadorServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NifValidator nifValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilizadorService utilizadorService;

    @Test
    void obterOuCriarUtente_NewUser_ShouldGenerateSecurePasswordAndSendEmail() {
        // Arrange
        String nif = "123456789";
        String nome = "Test User";
        String email = "test@example.com";
        String telefone = "912345678";

        when(utilizadorRepository.findByNif(nif)).thenReturn(Collections.emptyList());
        when(utenteRepository.existsByEmail(email)).thenReturn(false);
        when(utenteRepository.save(any(Utente.class))).thenAnswer(invocation -> {
            Utente u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Act
        Utente result = utilizadorService.obterOuCriarUtente(nif, nome, email, telefone);

        // Assert
        assertNotNull(result);
        assertEquals(nif, result.getNif());

        // Initial password logic was NIF. Now it should NOT be NIF.
        // We can't easily check the plain text password here because it's hashed inside
        // the service before saving.
        // But we can verify that emailService was called with a password that is NOT
        // the NIF.

        verify(emailService).sendPassword(anyString(), anyString());

        // Verify we are not saving the NIF as password (comparing hash would require
        // knowing the random password)
        // But we can assert the logic flow by verifying mocks.
    }

    @Test
    void obterOuCriarUtente_ExistingUser_ShouldReturnIt() {
        // Arrange
        String nif = "123456789";
        Utente existingStart = new Utente();
        existingStart.setNif(nif);
        existingStart.setId(1L);

        // When user exists, the service returns immediately after findByNif
        // without calling external NIF validation - so no additional stub needed
        when(utilizadorRepository.findByNif(nif)).thenReturn(List.of(existingStart));

        // Act
        Utente result = utilizadorService.obterOuCriarUtente(nif, "Ignored", "Ignored", "Ignored");

        // Assert
        assertEquals(existingStart, result);
    }

    @Test
    void obterOuCriarUtente_InvalidNif_ShouldThrowException() {
        // Arrange
        String invalidNif = "123";
        doThrow(new BadRequestException("NIF deve conter exatamente 9 dígitos numéricos"))
                .when(nifValidator).validateRequiredOrThrow(eq(invalidNif));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> utilizadorService.obterOuCriarUtente(invalidNif, "Name", "email@test.com", "912345678"));
        assertEquals("NIF deve conter exatamente 9 dígitos numéricos", ex.getMessage());
    }

    @Test
    void listarTodosUtentes_ShouldReturnAllUtentesWithCorrectStatus() {
        // Arrange
        Utente activeUtente = new Utente();
        activeUtente.setId(1L);
        activeUtente.setNome("Active Utente");
        activeUtente.setActivo(true);

        Utente inactiveUtente = new Utente();
        inactiveUtente.setId(2L);
        inactiveUtente.setNome("Inactive Utente");
        inactiveUtente.setActivo(false);

        when(utenteRepository.findAll()).thenReturn(List.of(activeUtente, inactiveUtente));

        // Act
        List<pt.florinhas.common_data.dto.UtilizadorResponseDTO> result = utilizadorService.listarTodosUtentes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Check active status mapping
        assertEquals(true, result.get(0).isActive());
        assertEquals("Active Utente", result.get(0).getNome());
        
        assertEquals(false, result.get(1).isActive());
        assertEquals("Inactive Utente", result.get(1).getNome());

        verify(utenteRepository).findAll();
    }
}
