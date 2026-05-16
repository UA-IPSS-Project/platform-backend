package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.dto.UtilizadorResponseDTO;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

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
    private AuditLogService auditLogService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private NifValidator nifValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private MarcacaoRepository marcacaoRepository;
    @Mock
    private CryptoUtils cryptoUtils;
    @Mock
    private ObjectProvider<UtilizadorService> selfProvider;

    private UtilizadorService utilizadorService;

    private static final String NIF = "123456789";
    private static final String NIF_HASH = "hashed_nif";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        utilizadorService = new UtilizadorService(
                utilizadorRepository,
                utenteRepository,
                funcionarioRepository,
                emailService,
                auditLogService,
                notificacaoService,
                nifValidator,
                passwordEncoder,
                documentoRepository,
                marcacaoRepository,
                cryptoUtils,
                selfProvider);
        
        when(selfProvider.getIfAvailable()).thenReturn(utilizadorService);
    }

    @Test
    @DisplayName("Deve carregar utilizador por ID")
    void obterUtilizadorPorId_ShouldReturnUser() {
        Utilizador user = new Utente();
        user.setId(1L);
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));

        Utilizador result = utilizadorService.obterUtilizadorPorId(1L);
        assertEquals(user, result);
    }

    @Test
    @DisplayName("Deve buscar utilizador por NIF usando Blind Index")
    void buscarPorNif_ShouldUseBlindIndex() {
        Utilizador user = new Utente();
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of(user));

        Optional<Utilizador> result = utilizadorService.buscarPorNif(NIF);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    @DisplayName("Deve obter ou criar utente quando já existe")
    void obterOuCriarUtente_WhenExists_ShouldReturnExisting() {
        Utente existing = new Utente();
        existing.setId(1L);
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of(existing));

        Utente result = utilizadorService.obterOuCriarUtente(NIF, "Nome", "email@test.com", "999");
        assertEquals(existing, result);
    }

    @Test
    @DisplayName("Deve lançar erro ao obter utente com NIF inválido")
    void obterOuCriarUtente_InvalidNif_ShouldThrowException() {
        doThrow(new BadRequestException("Erro NIF")).when(nifValidator).validateRequiredOrThrow(eq("123"));
        assertThrows(BadRequestException.class, () -> utilizadorService.obterOuCriarUtente("123", "N", "E", "P"));
    }

    @Test
    @DisplayName("Deve listar todos os utentes")
    void listarTodosUtentes_ShouldReturnList() {
        Utente u1 = new Utente();
        u1.setActivo(true);
        u1.setNome("A");
        Utente u2 = new Utente();
        u2.setActivo(false);
        u2.setNome("B");
        when(utenteRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UtilizadorResponseDTO> result = utilizadorService.listarTodosUtentes();
        assertEquals(2, result.size());
        assertTrue(result.get(0).isActive());
        assertFalse(result.get(1).isActive());
    }

    @Test
    @DisplayName("Deve atualizar utilizador com sucesso")
    void atualizarUtilizador_ShouldUpdateFields() {
        Utilizador user = new Utente();
        user.setId(1L);
        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setNome("Novo");
        dto.setEmail("new@test.com");
        dto.setDataNasc("2000-01-01");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(utilizadorRepository.findByEmail("new@test.com")).thenReturn(List.of());
        when(utilizadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Utilizador result = utilizadorService.atualizarUtilizador(1L, dto);
        assertEquals("Novo", result.getNome());
        assertEquals(LocalDate.of(2000, 1, 1), result.getDataNasc());
    }

    @Test
    @DisplayName("Deve aprovar funcionário pendente")
    void aprovarFuncionario_ShouldActivate() {
        Funcionario f = new Funcionario();
        f.setId(1L);
        f.setActivo(false);
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(f));

        utilizadorService.aprovarFuncionario(1L);
        assertTrue(f.isActivo());
        verify(funcionarioRepository).save(f);
    }

    @Test
    @DisplayName("Deve criar utilizador pela secretaria")
    void criarUtilizadorPelaSecretaria_ShouldCreateAndSendEmail() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif(NIF);
        dto.setEmail("test@test.com");
        dto.setRole("UTENTE");

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.existsByNifHash(NIF_HASH)).thenReturn(false);
        when(utilizadorRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(utilizadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Utilizador result = utilizadorService.criarUtilizadorPelaSecretaria(dto);
        assertNotNull(result);
        verify(emailService).sendPassword(eq("test@test.com"), anyString());
    }

    @Test
    @DisplayName("Deve recuperar conta e enviar nova senha")
    void recuperarConta_ShouldUpdateAndDeactivate() {
        Utente u = new Utente();
        u.setNif(NIF);
        u.setActivo(true);
        RecoverAccountDTO dto = new RecoverAccountDTO();
        dto.setNif(NIF);
        dto.setUpdatedEmail("new@test.com");

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of(u));
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");

        utilizadorService.recuperarConta(dto);
        assertEquals("new@test.com", u.getEmail());
        assertFalse(u.isActivo());
        verify(utilizadorRepository).save(u);
    }

    @Test
    @DisplayName("Deve contar utentes ativos")
    void contarUtentesAtivos_ShouldReturnCount() {
        when(utenteRepository.countByActivo(true)).thenReturn(10L);
        assertEquals(10L, utilizadorService.contarUtentesAtivos());
    }
}