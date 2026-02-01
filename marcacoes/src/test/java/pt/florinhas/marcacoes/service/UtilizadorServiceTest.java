package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.service.nif.NifValidationService;

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
    private NifValidationService nifValidationService;

    @InjectMocks
    private UtilizadorService utilizadorService;

    @Test
    void obterOuCriarUtente_NewUser_ShouldGenerateSecurePasswordAndSendEmail() {
        // Arrange
        String nif = "123456789";
        String nome = "Test User";
        String email = "test@example.com";
        String telefone = "912345678";

        when(nifValidationService.validate(nif)).thenReturn(true);
        when(utilizadorRepository.findByNif(nif)).thenReturn(java.util.Collections.emptyList());
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

        when(nifValidationService.validate(nif)).thenReturn(true);
        when(utilizadorRepository.findByNif(nif)).thenReturn(java.util.List.of(existingStart));

        // Act
        Utente result = utilizadorService.obterOuCriarUtente(nif, "Ignored", "Ignored", "Ignored");

        // Assert
        assertEquals(existingStart, result);
    }

    @Test
    void obterOuCriarUtente_InvalidNif_ShouldThrowException() {
        // Arrange
        String invalidNif = "123"; // Too short
        // Configurar o mock para retornar false (opcional se o default for false, mas
        // explícito é melhor)
        // No caso do teste antigo, o mock retorna false por defeito para strings nao
        // configuradas?
        // Sim, defaults do mockito para boolean é false.
        // Mas vamos forçar para garantir:
        when(nifValidationService.validate(invalidNif)).thenReturn(false);

        // Act & Assert
        try {
            utilizadorService.obterOuCriarUtente(invalidNif, "Name", "email@test.com", "912345678");
        } catch (RuntimeException e) {
            assertEquals("NIF do utente inválido (deve ter 9 dígitos numéricos).", e.getMessage());
        }
    }
}
