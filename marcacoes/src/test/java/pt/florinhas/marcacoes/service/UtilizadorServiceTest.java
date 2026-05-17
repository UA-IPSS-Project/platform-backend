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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
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
import pt.florinhas.marcacoes.exception.ConflictException;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceTest {

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
        
        lenient().when(selfProvider.getIfAvailable()).thenReturn(utilizadorService);
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
    @DisplayName("Deve buscar utilizador por email quando existe")
    void buscarPorEmail_WhenFound_ShouldReturnUser() {
        Utilizador user = new Utente();
        when(utilizadorRepository.findByEmail("test@test.com")).thenReturn(List.of(user));

        Utilizador result = utilizadorService.buscarPorEmail("test@test.com");
        assertEquals(user, result);
    }

    @Test
    @DisplayName("Deve lancar NotFoundException ao buscar por email inexistente")
    void buscarPorEmail_WhenNotFound_ShouldThrowNotFoundException() {
        when(utilizadorRepository.findByEmail("notfound@test.com")).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> utilizadorService.buscarPorEmail("notfound@test.com"));
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
    @DisplayName("buscarPorNif com nif nulo ou vazio deve retornar Optional vazio")
    void buscarPorNif_WhenNifNullOrEmpty_ShouldReturnEmpty() {
        assertTrue(utilizadorService.buscarPorNif(null).isEmpty());
        assertTrue(utilizadorService.buscarPorNif("   ").isEmpty());
    }

    @Test
    @DisplayName("buscarPorNif nao encontrando deve retornar Optional vazio")
    void buscarPorNif_WhenNotFound_ShouldReturnEmpty() {
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of());

        assertTrue(utilizadorService.buscarPorNif(NIF).isEmpty());
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
    @DisplayName("obterOuCriarUtente deve falhar quando NIF já está associado a Funcionario")
    void obterOuCriarUtente_WhenNifIsFuncionario_ShouldThrowConflictException() {
        Funcionario existingFunc = new Funcionario();
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of(existingFunc));

        assertThrows(ConflictException.class, () ->
                utilizadorService.obterOuCriarUtente(NIF, "Nome", "email@test.com", "999")
        );
    }

    @Test
    @DisplayName("obterOuCriarUtente deve falhar quando email ja esta registado")
    void obterOuCriarUtente_WhenEmailExists_ShouldThrowConflictException() {
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of());
        when(utenteRepository.existsByEmail("exist@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                utilizadorService.obterOuCriarUtente(NIF, "Nome", "exist@test.com", "999")
        );
    }

    @Test
    @DisplayName("obterOuCriarUtente deve auto-criar utente com sucesso")
    void obterOuCriarUtente_WhenNotExists_ShouldCreateNewUtente() {
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of());
        when(utenteRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(utenteRepository.save(any(Utente.class))).thenAnswer(i -> {
            Utente saved = i.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Utente result = utilizadorService.obterOuCriarUtente(NIF, "Novo Utente", "new@test.com", "987654321");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Novo Utente", result.getNome());
        assertFalse(result.isActivo());
        verify(emailService).sendPassword(eq("new@test.com"), anyString());
    }

    @Test
    @DisplayName("Deve lançar erro ao obter utente com NIF inválido")
    void obterOuCriarUtente_InvalidNif_ShouldThrowException() {
        doThrow(new BadRequestException("Erro NIF")).when(nifValidator).validateRequiredOrThrow("123");
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
    @DisplayName("atualizarUtilizador deve falhar com formato de data de nascimento invalido")
    void atualizarUtilizador_InvalidBirthDate_ShouldThrowBadRequestException() {
        Utilizador user = new Utente();
        user.setId(1L);
        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setDataNasc("01-01-2000"); // Invalid ISO format

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> utilizadorService.atualizarUtilizador(1L, dto));
    }

    @Test
    @DisplayName("atualizarUtilizador deve falhar se o novo email estiver em uso por outro utilizador")
    void atualizarUtilizador_EmailInUseByOther_ShouldThrowConflictException() {
        Utilizador user = new Utente();
        user.setId(1L);

        Utilizador otherUser = new Utente();
        otherUser.setId(2L);

        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setEmail("other@test.com");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(utilizadorRepository.findByEmail("other@test.com")).thenReturn(List.of(otherUser));

        assertThrows(ConflictException.class, () -> utilizadorService.atualizarUtilizador(1L, dto));
    }

    @Test
    @DisplayName("atualizarUtilizador deve atualizar todos os campos opcionais")
    void atualizarUtilizador_ShouldUpdateAllOptionalFields() {
        Utilizador user = new Utente();
        user.setId(1L);
        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setTelefone("919191919");
        dto.setMorada("Rua A");
        dto.setCodigoPostal("1000-000");
        dto.setFreguesia("Freguesia A");
        dto.setTelefoneEmprego("212121212");
        dto.setLocalEmprego("Empresa X");
        dto.setMoradaEmprego("Morada Job");
        dto.setProfissao("Engenheiro");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(utilizadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Utilizador result = utilizadorService.atualizarUtilizador(1L, dto);

        assertEquals("919191919", result.getTelefone());
        assertEquals("Rua A", result.getMorada());
        assertEquals("1000-000", result.getCodigoPostal());
        assertEquals("Freguesia A", result.getFreguesia());
        assertEquals("212121212", result.getTelefoneEmprego());
        assertEquals("Empresa X", result.getLocalEmprego());
        assertEquals("Morada Job", result.getMoradaEmprego());
        assertEquals("Engenheiro", result.getProfissao());
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
    @DisplayName("listarTodosFuncionarios deve retornar lista mapeada")
    void listarTodosFuncionarios_ShouldReturnList() {
        Funcionario f = new Funcionario();
        f.setId(10L);
        f.setNome("Func");
        when(funcionarioRepository.findAll()).thenReturn(List.of(f));

        List<UtilizadorResponseDTO> result = utilizadorService.listarTodosFuncionarios();
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    @DisplayName("pesquisarFuncionarios deve retornar pagina filtrada")
    void pesquisarFuncionarios_ShouldReturnPage() {
        Funcionario f = new Funcionario();
        f.setId(10L);
        Page<Funcionario> page = new PageImpl<>(List.of(f));
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(funcionarioRepository.findByNomeAndTipoFilter("Nome", FuncionarioTipo.SECRETARIA, NIF_HASH, PageRequest.of(0, 10))).thenReturn(page);

        Page<UtilizadorResponseDTO> result = utilizadorService.pesquisarFuncionarios("Nome", FuncionarioTipo.SECRETARIA, NIF, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("listarFuncionariosPendentes deve retornar lista")
    void listarFuncionariosPendentes_ShouldReturnList() {
        Funcionario f = new Funcionario();
        f.setActivo(false);
        when(funcionarioRepository.findByActivoFalse()).thenReturn(List.of(f));

        List<UtilizadorResponseDTO> result = utilizadorService.listarFuncionariosPendentes();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("pesquisarUtentes deve retornar pagina filtrada")
    void pesquisarUtentes_ShouldReturnPage() {
        Utente u = new Utente();
        Page<Utente> page = new PageImpl<>(List.of(u));
        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utenteRepository.findByNomeFilter("Nome", NIF_HASH, PageRequest.of(0, 10))).thenReturn(page);

        Page<UtilizadorResponseDTO> result = utilizadorService.pesquisarUtentes("Nome", NIF, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("criarUtilizadorPelaSecretaria deve falhar quando NIF ja existe")
    void criarUtilizadorPelaSecretaria_NifExists_ShouldThrowConflictException() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif(NIF);

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.existsByNifHash(NIF_HASH)).thenReturn(true);

        assertThrows(ConflictException.class, () -> utilizadorService.criarUtilizadorPelaSecretaria(dto));
    }

    @Test
    @DisplayName("criarUtilizadorPelaSecretaria deve falhar quando Email ja existe")
    void criarUtilizadorPelaSecretaria_EmailExists_ShouldThrowConflictException() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif(NIF);
        dto.setEmail("email@test.com");

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.existsByNifHash(NIF_HASH)).thenReturn(false);
        when(utilizadorRepository.existsByEmail("email@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> utilizadorService.criarUtilizadorPelaSecretaria(dto));
    }

    @Test
    @DisplayName("criarUtilizadorPelaSecretaria deve criar funcionario com mapeamento de cargos")
    void criarUtilizadorPelaSecretaria_ShouldCreateFuncionario() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif(NIF);
        dto.setEmail("emp@test.com");
        dto.setRole("BALNEARIO");
        dto.setEmployee(true);

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.existsByNifHash(NIF_HASH)).thenReturn(false);
        when(utilizadorRepository.existsByEmail("emp@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(utilizadorRepository.save(any(Funcionario.class))).thenAnswer(i -> {
            Funcionario saved = i.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        Utilizador result = utilizadorService.criarUtilizadorPelaSecretaria(dto);

        assertNotNull(result);
        assertTrue(result instanceof Funcionario);
        assertEquals(FuncionarioTipo.BALNEARIO, ((Funcionario) result).getTipo());
        verify(emailService).sendPassword(eq("emp@test.com"), anyString());
    }

    @Test
    @DisplayName("criarUtilizadorPelaSecretaria com cargo desconhecido deve criar funcionario OUTRO")
    void criarUtilizadorPelaSecretaria_InvalidRole_ShouldCreateFuncionarioWithOutro() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif(NIF);
        dto.setEmail("emp@test.com");
        dto.setRole("GERENTE"); // Desconhecido
        dto.setEmployee(true);

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.existsByNifHash(NIF_HASH)).thenReturn(false);
        when(utilizadorRepository.existsByEmail("emp@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(utilizadorRepository.save(any(Funcionario.class))).thenAnswer(i -> i.getArgument(0));

        Utilizador result = utilizadorService.criarUtilizadorPelaSecretaria(dto);

        assertNotNull(result);
        assertTrue(result instanceof Funcionario);
        assertEquals(FuncionarioTipo.OUTRO, ((Funcionario) result).getTipo());
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
    @DisplayName("recuperarConta deve falhar quando NIF nao for encontrado")
    void recuperarConta_UserNotFound_ShouldThrowNotFoundException() {
        RecoverAccountDTO dto = new RecoverAccountDTO();
        dto.setNif(NIF);

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> utilizadorService.recuperarConta(dto));
    }

    @Test
    @DisplayName("recuperarConta deve falhar quando o novo email ja esta associado a outra conta")
    void recuperarConta_EmailInUse_ShouldThrowConflictException() {
        Utente u = new Utente();
        u.setNif(NIF);
        u.setEmail("old@test.com");

        RecoverAccountDTO dto = new RecoverAccountDTO();
        dto.setNif(NIF);
        dto.setUpdatedEmail("inuse@test.com");

        when(cryptoUtils.generateBlindIndex(NIF)).thenReturn(NIF_HASH);
        when(utilizadorRepository.findByNifHash(NIF_HASH)).thenReturn(List.of(u));
        when(utilizadorRepository.existsByEmail("inuse@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> utilizadorService.recuperarConta(dto));
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