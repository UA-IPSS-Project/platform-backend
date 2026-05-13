package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
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

class UtilizadorServiceTest {

    private UtilizadorRepository utilizadorRepository;
    private UtenteRepository utenteRepository;
    private FuncionarioRepository funcionarioRepository;
    private EmailService emailService;
    private AuditLogService auditLogService;
    private NotificacaoService notificacaoService;
    private NifValidator nifValidator;
    private PasswordEncoder passwordEncoder;
    private DocumentoRepository documentoRepository;
    private MarcacaoRepository marcacaoRepository;
    private CryptoUtils cryptoUtils;

    private UtilizadorService service;

    @BeforeEach
    void setup() {

        utilizadorRepository =
                mock(UtilizadorRepository.class);

        utenteRepository =
                mock(UtenteRepository.class);

        funcionarioRepository =
                mock(FuncionarioRepository.class);

        emailService =
                mock(EmailService.class);

        auditLogService =
                mock(AuditLogService.class);

        notificacaoService =
                mock(NotificacaoService.class);

        nifValidator =
                mock(NifValidator.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        documentoRepository =
                mock(DocumentoRepository.class);

        marcacaoRepository =
                mock(MarcacaoRepository.class);

        cryptoUtils =
                mock(CryptoUtils.class);

        service =
                new UtilizadorService();

        ReflectionTestUtils.setField(
                service,
                "utilizadorRepository",
                utilizadorRepository
        );

        ReflectionTestUtils.setField(
                service,
                "utenteRepository",
                utenteRepository
        );

        ReflectionTestUtils.setField(
                service,
                "funcionarioRepository",
                funcionarioRepository
        );

        ReflectionTestUtils.setField(
                service,
                "emailService",
                emailService
        );

        ReflectionTestUtils.setField(
                service,
                "auditLogService",
                auditLogService
        );

        ReflectionTestUtils.setField(
                service,
                "notificacaoService",
                notificacaoService
        );

        ReflectionTestUtils.setField(
                service,
                "nifValidator",
                nifValidator
        );

        ReflectionTestUtils.setField(
                service,
                "passwordEncoder",
                passwordEncoder
        );

        ReflectionTestUtils.setField(
                service,
                "documentoRepository",
                documentoRepository
        );

        ReflectionTestUtils.setField(
                service,
                "marcacaoRepository",
                marcacaoRepository
        );

        ReflectionTestUtils.setField(
                service,
                "cryptoUtils",
                cryptoUtils
        );
    }

    @Test
    void buscarPorEmail_DeveRetornarUtilizador() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail("teste@test.com");

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(utilizador));

        Utilizador resultado =
                service.buscarPorEmail("teste@test.com");

        assertEquals(
                "teste@test.com",
                resultado.getEmail()
        );
    }

    @Test
    void buscarPorEmail_DeveLancarNotFound() {

        when(utilizadorRepository.findByEmail("x@test.com"))
                .thenReturn(List.of());

        assertThrows(
                NotFoundException.class,
                () -> service.buscarPorEmail("x@test.com")
        );
    }

    @Test
    void buscarPorNif_DeveRetornarEmptyQuandoNifNull() {

        Optional<Utilizador> resultado =
                service.buscarPorNif(null);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNif_DeveRetornarEmptyQuandoNifVazio() {

        Optional<Utilizador> resultado =
                service.buscarPorNif("   ");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNif_DeveRetornarUtilizador() {

        Utilizador utilizador =
                new Utilizador();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(utilizador));

        Optional<Utilizador> resultado =
                service.buscarPorNif("123456789");

        assertTrue(resultado.isPresent());
    }

    @Test
    void buscarPorNif_DeveRetornarEmptyQuandoNaoExiste() {

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        Optional<Utilizador> resultado =
                service.buscarPorNif("123456789");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obterOuCriarUtente_DeveRetornarUtenteExistente() {

        Utente utente =
                new Utente();

        utente.setId(1L);

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(utente));

        Utente resultado =
                service.obterOuCriarUtente(
                        "123456789",
                        "Joao",
                        "joao@test.com",
                        "912345678"
                );

        assertEquals(
                1L,
                resultado.getId()
        );

        verify(utenteRepository, never())
                .save(any());
    }

    @Test
    void obterOuCriarUtente_DeveLancarConflitoQuandoNifEDeFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(funcionario));

        assertThrows(
                ConflictException.class,
                () -> service.obterOuCriarUtente(
                        "123456789",
                        "Joao",
                        "joao@test.com",
                        "912345678"
                )
        );
    }

    @Test
    void obterOuCriarUtente_DeveCriarNovoUtente() {

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        when(utenteRepository.existsByEmail("joao@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utenteRepository.save(any(Utente.class)))
                .thenAnswer(invocation -> {
                    Utente u = invocation.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        Utente resultado =
                service.obterOuCriarUtente(
                        "123456789",
                        "Joao",
                        "joao@test.com",
                        "912345678"
                );

        assertEquals(
                1L,
                resultado.getId()
        );

        assertEquals(
                "Joao",
                resultado.getNome()
        );

        assertEquals(
                "encoded",
                resultado.getPassHash()
        );

        assertFalse(
                resultado.isActivo()
        );

        verify(emailService)
                .sendPassword(
                        eq("joao@test.com"),
                        anyString()
                );
    }

    @Test
    void obterOuCriarUtente_DeveLancarErroQuandoEmailJaExiste() {

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        when(utenteRepository.existsByEmail("joao@test.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.obterOuCriarUtente(
                        "123456789",
                        "Joao",
                        "joao@test.com",
                        "912345678"
                )
        );
    }

    @Test
    void obterUtilizadorPorId_DeveRetornarUtilizador() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setId(1L);

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(utilizador));

        Utilizador resultado =
                service.obterUtilizadorPorId(1L);

        assertEquals(
                1L,
                resultado.getId()
        );
    }

    @Test
    void obterUtilizadorPorId_DeveLancarNotFound() {

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.obterUtilizadorPorId(1L)
        );
    }

    @Test
    void aprovarFuncionario_DeveAtivarFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Maria");
        funcionario.setEmail("maria@test.com");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario.setActivo(false);

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        service.aprovarFuncionario(1L);

        assertTrue(
                funcionario.isActivo()
        );

        verify(funcionarioRepository)
                .save(funcionario);

        verify(auditLogService)
                .log(
                        eq("APROVAR_FUNCIONARIO"),
                        eq("FUNCIONARIO"),
                        eq(1L),
                        contains("Maria")
                );
    }

    @Test
    void aprovarFuncionario_DeveLancarNotFound() {

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.aprovarFuncionario(1L)
        );
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveCriarUtente() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setName("Joao");
        request.setNif("123456789");
        request.setContact("912345678");
        request.setEmail("joao@test.com");
        request.setBirthDate("2000-01-01");
        request.setEmployee(false);
        request.setRole("UTENTE");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.existsByNifHash("hash"))
                .thenReturn(false);

        when(utilizadorRepository.existsByEmail("joao@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utilizadorRepository.save(any(Utilizador.class)))
                .thenAnswer(invocation -> {
                    Utilizador u = invocation.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        Utilizador resultado =
                service.criarUtilizadorPelaSecretaria(request);

        assertTrue(
                resultado instanceof Utente
        );

        assertEquals(
                "Joao",
                resultado.getNome()
        );

        verify(emailService)
                .sendPassword(
                        eq("joao@test.com"),
                        anyString()
                );
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveCriarFuncionarioSecretaria() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setName("Maria");
        request.setNif("123456789");
        request.setContact("912345678");
        request.setEmail("maria@test.com");
        request.setBirthDate("2000-01-01");
        request.setEmployee(true);
        request.setRole("SECRETARIA");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.existsByNifHash("hash"))
                .thenReturn(false);

        when(utilizadorRepository.existsByEmail("maria@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utilizadorRepository.save(any(Utilizador.class)))
                .thenAnswer(invocation -> {
                    Utilizador u = invocation.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        Utilizador resultado =
                service.criarUtilizadorPelaSecretaria(request);

        assertTrue(
                resultado instanceof Funcionario
        );

        assertEquals(
                FuncionarioTipo.SECRETARIA,
                ((Funcionario) resultado).getTipo()
        );
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveLancarConflitoNifDuplicado() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setNif("123456789");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.existsByNifHash("hash"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.criarUtilizadorPelaSecretaria(request)
        );
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveLancarConflitoEmailDuplicado() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setNif("123456789");
        request.setEmail("x@test.com");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.existsByNifHash("hash"))
                .thenReturn(false);

        when(utilizadorRepository.existsByEmail("x@test.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.criarUtilizadorPelaSecretaria(request)
        );
    }

    @Test
    void recuperarConta_DeveAtualizarDados() {

        RecoverAccountDTO request =
                new RecoverAccountDTO();

        request.setNif("123456789");
        request.setUpdatedEmail("novo@test.com");
        request.setUpdatedContact("912345678");

        Utente utilizador =
                new Utente();

        utilizador.setId(1L);
        utilizador.setNome("Joao");
        utilizador.setNif("123456789");
        utilizador.setEmail("antigo@test.com");
        utilizador.setTelefone("900000000");
        utilizador.setActivo(true);

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(utilizador));

        when(utilizadorRepository.existsByEmail("novo@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        service.recuperarConta(request);

        assertEquals(
                "novo@test.com",
                utilizador.getEmail()
        );

        assertEquals(
                "912345678",
                utilizador.getTelefone()
        );

        assertEquals(
                "encoded",
                utilizador.getPassHash()
        );

        assertFalse(
                utilizador.isActivo()
        );

        verify(utilizadorRepository)
                .save(utilizador);

        verify(emailService)
                .sendPassword(
                        eq("novo@test.com"),
                        anyString()
                );
    }

    @Test
    void recuperarConta_DeveLancarNotFound() {

        RecoverAccountDTO request =
                new RecoverAccountDTO();

        request.setNif("123456789");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(
                NotFoundException.class,
                () -> service.recuperarConta(request)
        );
    }

    @Test
    void recuperarConta_DeveLancarConflitoEmail() {

        RecoverAccountDTO request =
                new RecoverAccountDTO();

        request.setNif("123456789");
        request.setUpdatedEmail("novo@test.com");

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail("antigo@test.com");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(utilizador));

        when(utilizadorRepository.existsByEmail("novo@test.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.recuperarConta(request)
        );
    }

    @Test
    void contarUtentesAtivos_DeveRetornarTotal() {

        when(utenteRepository.countByActivo(true))
                .thenReturn(5L);

        long resultado =
                service.contarUtentesAtivos();

        assertEquals(
                5L,
                resultado
        );
    }

    @Test
    void anonimizarUtilizador_DeveAnonimizarUtente() {

        Utente utente =
                new Utente();

        utente.setId(1L);
        utente.setNome("Joao");
        utente.setEmail("joao@test.com");
        utente.setTelefone("912345678");
        utente.setNif("123456789");
        utente.setActivo(true);
        utente.setDeleteRequested(true);

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        service.anonimizarUtilizador(1L);

        assertEquals(
                "Utilizador Anónimo #1",
                utente.getNome()
        );

        assertEquals(
                "000000000",
                utente.getTelefone()
        );

        assertEquals(
                "000000001",
                utente.getNif()
        );

        assertEquals(
                "encoded",
                utente.getPassHash()
        );

        assertFalse(
                utente.isActivo()
        );

        assertFalse(
                utente.getDeleteRequested()
        );

        assertNull(
                utente.getDeleteRequestedAt()
        );

        verify(utilizadorRepository)
                .save(utente);
    }

    @Test
    void anonimizarEEliminarUtilizador_DeveExecutar() {

        Utente utente =
                new Utente();

        utente.setId(1L);
        utente.setNome("Joao");
        utente.setNif("123456789");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        service.anonimizarEEliminarUtilizador(1L);

        verify(auditLogService)
                .log(
                        eq("ELIMINAR_UTILIZADOR"),
                        eq("UTILIZADOR"),
                        eq(1L),
                        contains("Joao")
                );
    }
}