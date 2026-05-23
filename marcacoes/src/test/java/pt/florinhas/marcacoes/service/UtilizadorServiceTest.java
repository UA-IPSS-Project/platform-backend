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
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
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

    @Mock
    private CryptoUtils cryptoUtils;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private MarcacaoRepository marcacaoRepository;

    @InjectMocks
    private UtilizadorService utilizadorService;

    private static final String NIF = "123456789";
    private static final String NIF_HASH = "hash_123456789";

    @Test
    void obterOuCriarUtente_NewUser_ShouldGenerateSecurePasswordAndSendEmail() {
        String nome = "Test User";
        String email = "test@example.com";
        String telefone = "912345678";

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(Collections.emptyList());
        when(utenteRepository.existsByEmail(email)).thenReturn(false);
        when(utenteRepository.save(any(Utente.class))).thenAnswer(invocation -> {
            Utente u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        Utente result = utilizadorService.obterOuCriarUtente(NIF, nome, email, telefone);

        assertNotNull(result);
        assertEquals(NIF, result.getNif());
        verify(emailService).sendPassword(anyString(), anyString());
    }

    @Test
    void obterOuCriarUtente_ExistingUser_ShouldReturnIt() {
        Utente existing = new Utente();
        existing.setNif(NIF);
        existing.setId(1L);

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of(existing));

        Utente result = utilizadorService.obterOuCriarUtente(NIF, "Ignored", "Ignored", "Ignored");

        assertEquals(existing, result);
    }

    @Test
    void obterOuCriarUtente_InvalidNif_ShouldThrowException() {
        String invalidNif = "123";
        doThrow(new BadRequestException("NIF deve conter exatamente 9 dígitos numéricos"))
                .when(nifValidator).validateRequiredOrThrow(eq(invalidNif));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> utilizadorService.obterOuCriarUtente(invalidNif, "Name", "email@test.com", "912345678"));
        assertEquals("NIF deve conter exatamente 9 dígitos numéricos", ex.getMessage());
    }

    @Test
    void listarTodosUtentes_ShouldReturnAllUtentesWithCorrectStatus() {
        Utente activeUtente = new Utente();
        activeUtente.setId(1L);
        activeUtente.setNome("Active Utente");
        activeUtente.setActivo(true);

        Utente inactiveUtente = new Utente();
        inactiveUtente.setId(2L);
        inactiveUtente.setNome("Inactive Utente");
        inactiveUtente.setActivo(false);

        when(utenteRepository.findAll()).thenReturn(List.of(activeUtente, inactiveUtente));

        List<pt.florinhas.common_data.dto.UtilizadorResponseDTO> result = utilizadorService.listarTodosUtentes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(true, result.get(0).isActive());
        assertEquals("Active Utente", result.get(0).getNome());
        assertEquals(false, result.get(1).isActive());
        assertEquals("Inactive Utente", result.get(1).getNome());
        verify(utenteRepository).findAll();
    }
}
