package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
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
    void setUp() throws Exception {

        utilizadorRepository = mock(UtilizadorRepository.class);
        utenteRepository = mock(UtenteRepository.class);
        funcionarioRepository = mock(FuncionarioRepository.class);
        emailService = mock(EmailService.class);
        auditLogService = mock(AuditLogService.class);
        notificacaoService = mock(NotificacaoService.class);
        nifValidator = mock(NifValidator.class);
        passwordEncoder = mock(PasswordEncoder.class);
        documentoRepository = mock(DocumentoRepository.class);
        marcacaoRepository = mock(MarcacaoRepository.class);
        cryptoUtils = mock(CryptoUtils.class);

        service = new UtilizadorService();

        setField("utilizadorRepository", utilizadorRepository);
        setField("utenteRepository", utenteRepository);
        setField("funcionarioRepository", funcionarioRepository);
        setField("emailService", emailService);
        setField("auditLogService", auditLogService);
        setField("notificacaoService", notificacaoService);
        setField("nifValidator", nifValidator);
        setField("passwordEncoder", passwordEncoder);
        setField("documentoRepository", documentoRepository);
        setField("marcacaoRepository", marcacaoRepository);
        setField("cryptoUtils", cryptoUtils);
    }

    @Test
    void buscarPorEmail_DeveRetornarUtilizador() {

        Utente user = criarUtente();

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(user));

        Utilizador result = service.buscarPorEmail("teste@test.com");

        assertEquals("Nuno", result.getNome());
    }

    @Test
    void buscarPorEmail_DeveLancarErro() {

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class, () ->
                service.buscarPorEmail("teste@test.com"));
    }

    @Test
    void buscarPorNif_DeveRetornarUtilizador() {

        Utente user = criarUtente();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        Optional<Utilizador> result = service.buscarPorNif("123456789");

        assertTrue(result.isPresent());
    }

    @Test
    void buscarPorNif_DeveRetornarEmpty() {

        Optional<Utilizador> result = service.buscarPorNif("");

        assertTrue(result.isEmpty());
    }

    @Test
    void obterOuCriarUtente_DeveRetornarExistente() {

        Utente user = criarUtente();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        Utente result = service.obterOuCriarUtente(
                "123456789",
                "Nuno",
                "teste@test.com",
                "999999999");

        assertEquals("Nuno", result.getNome());
    }

    @Test
    void obterOuCriarUtente_DeveLancarErroQuandoFuncionario() {

        Funcionario funcionario = new Funcionario();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(funcionario));

        assertThrows(ConflictException.class, () ->
                service.obterOuCriarUtente(
                        "123456789",
                        "Nuno",
                        "teste@test.com",
                        "999999999"));
    }

    @Test
    void obterOuCriarUtente_DeveCriarNovo() {

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utenteRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Utente result = service.obterOuCriarUtente(
                "123456789",
                "Nuno",
                "teste@test.com",
                "999999999");

        assertEquals("Nuno", result.getNome());

        verify(emailService).sendPassword(
                any(),
                any());
    }

    @Test
    void obterOuCriarUtente_DeveLancarErroEmailDuplicado() {

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        when(utenteRepository.existsByEmail("teste@test.com"))
                .thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.obterOuCriarUtente(
                        "123456789",
                        "Nuno",
                        "teste@test.com",
                        "999999999"));
    }

    @Test
    void obterUtilizadorPorId_DeveRetornar() {

        Utente user = criarUtente();

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Utilizador result = service.obterUtilizadorPorId(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void obterUtilizadorPorId_DeveLancarErro() {

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.obterUtilizadorPorId(1L));
    }

    @Test
    void atualizarUtilizador_DeveAtualizarCampos() {

        Utente user = criarUtente();

        UtilizadorInfoDTO dto = mock(UtilizadorInfoDTO.class);

        when(dto.getNome()).thenReturn("Novo");
        when(dto.getTelefone()).thenReturn("911111111");
        when(dto.getMorada()).thenReturn("Rua");
        when(dto.getCodigoPostal()).thenReturn("1234");
        when(dto.getFreguesia()).thenReturn("Aveiro");
        when(dto.getProfissao()).thenReturn("Dev");
        when(dto.getEmail()).thenReturn("novo@test.com");
        when(dto.getDataNasc()).thenReturn("2000-01-01");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(utilizadorRepository.findByEmail("novo@test.com"))
                .thenReturn(List.of());

        when(utilizadorRepository.save(user))
                .thenReturn(user);

        Utilizador result = service.atualizarUtilizador(1L, dto);

        assertEquals("Novo", result.getNome());
        assertEquals("911111111", result.getTelefone());
    }

    @Test
    void atualizarUtilizador_DeveLancarErroEmailDuplicado() {

        Utente user = criarUtente();
        Utente other = criarUtente();

        other.setId(2L);

        UtilizadorInfoDTO dto = mock(UtilizadorInfoDTO.class);

        when(dto.getEmail()).thenReturn("duplicado@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(utilizadorRepository.findByEmail("duplicado@test.com"))
                .thenReturn(List.of(other));

        assertThrows(ConflictException.class, () ->
                service.atualizarUtilizador(1L, dto));
    }

    @Test
    void atualizarUtilizador_DeveLancarErroData() {

        Utente user = criarUtente();

        UtilizadorInfoDTO dto = mock(UtilizadorInfoDTO.class);

        when(dto.getDataNasc()).thenReturn("erro");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () ->
                service.atualizarUtilizador(1L, dto));
    }

    @Test
    void listarTodosFuncionarios_DeveRetornarLista() {

        when(funcionarioRepository.findAll())
                .thenReturn(List.of(new Funcionario()));

        assertEquals(1, service.listarTodosFuncionarios().size());
    }

    @Test
    void pesquisarFuncionarios_DeveRetornarPagina() {

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(funcionarioRepository.findByNomeAndTipoFilter(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(new PageImpl<>(
                        List.of(new Funcionario()),
                        PageRequest.of(0, 10),
                        1));

        assertEquals(
                1,
                service.pesquisarFuncionarios(
                        null,
                        null,
                        "123",
                        PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void listarFuncionariosPendentes_DeveRetornarLista() {

        when(funcionarioRepository.findByActivoFalse())
                .thenReturn(List.of(new Funcionario()));

        assertEquals(1, service.listarFuncionariosPendentes().size());
    }

    @Test
    void listarTodosUtentes_DeveRetornarLista() {

        when(utenteRepository.findAll())
                .thenReturn(List.of(criarUtente()));

        assertEquals(1, service.listarTodosUtentes().size());
    }

    @Test
    void pesquisarUtentes_DeveRetornarPagina() {

        when(cryptoUtils.generateBlindIndex("123"))
                .thenReturn("hash");

        when(utenteRepository.findByNomeFilter(
                any(),
                any(),
                any()))
                .thenReturn(new PageImpl<>(
                        List.of(criarUtente()),
                        PageRequest.of(0, 10),
                        1));

        assertEquals(
                1,
                service.pesquisarUtentes(
                        null,
                        "123",
                        PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void aprovarFuncionario_DeveAprovar() {

        Funcionario funcionario = new Funcionario();

        funcionario.setId(1L);

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        service.aprovarFuncionario(1L);

        assertTrue(funcionario.isActivo());

        verify(funcionarioRepository).save(funcionario);
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveCriarUtente() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setNif("123456789");
        request.setName("Nuno");
        request.setContact("999999999");
        request.setEmail("teste@test.com");
        request.setBirthDate("2000-01-01");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utilizadorRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador result =
                service.criarUtilizadorPelaSecretaria(request);

        assertTrue(result instanceof Utente);

        verify(emailService).sendPassword(
                any(),
                any());
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveCriarFuncionario() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setEmployee(true);
        request.setRole("SECRETARIA");
        request.setNif("123456789");
        request.setName("Nuno");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utilizadorRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador result =
                service.criarUtilizadorPelaSecretaria(request);

        assertTrue(result instanceof Funcionario);

        assertEquals(
                FuncionarioTipo.SECRETARIA,
                ((Funcionario) result).getTipo());
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveLancarErroNifDuplicado() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setNif("123456789");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.existsByNifHash("hash"))
                .thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.criarUtilizadorPelaSecretaria(request));
    }

    @Test
    void recuperarConta_DeveAtualizarConta() {

        RecoverAccountDTO dto =
                new RecoverAccountDTO();

        dto.setNif("123456789");
        dto.setUpdatedEmail("novo@test.com");
        dto.setUpdatedContact("911111111");

        Utente user = criarUtente();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        service.recuperarConta(dto);

        assertFalse(user.isActivo());

        verify(utilizadorRepository).save(user);
    }

    @Test
    void recuperarConta_DeveLancarErroNaoEncontrado() {

        RecoverAccountDTO dto =
                new RecoverAccountDTO();

        dto.setNif("123456789");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class, () ->
                service.recuperarConta(dto));
    }

    @Test
    void contarUtentesAtivos_DeveRetornarValor() {

        when(utenteRepository.countByActivo(true))
                .thenReturn(10L);

        assertEquals(10L, service.contarUtentesAtivos());
    }

    @Test
    void contarFuncionariosAtivos_DeveRetornarValor() {

        when(funcionarioRepository.countByActivo(true))
                .thenReturn(5L);

        assertEquals(5L, service.contarFuncionariosAtivos());
    }

    @Test
    void gerarCodigoPresencial_DeveGerar() {

        Utente user = criarUtente();

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utilizadorRepository.findByNifHash("hash"))
                .thenReturn(List.of(user));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        String result =
                service.gerarCodigoPresencial("123456789");

        assertNotNull(result);

        assertEquals(6, result.length());

        verify(utilizadorRepository).save(user);
    }

    @Test
    void solicitarEliminacaoConta_DeveSolicitar() {

        Utente user = criarUtente();

        Funcionario secretaria =
                new Funcionario();

        secretaria.setId(2L);

        mockAuth(user);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(user));

        when(funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA))
                .thenReturn(List.of(secretaria));

        service.solicitarEliminacaoConta();

        assertTrue(user.getDeleteRequested());

        verify(notificacaoService).criarNotificacao(
                any(),
                any(),
                any(),
                any());

        verify(emailService).sendGenericEmail(
                any(),
                any(),
                any());
    }

    @Test
    void solicitarEliminacaoConta_DeveLancarErroDuplicado() {

        Utente user = criarUtente();

        user.setDeleteRequested(true);

        mockAuth(user);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(user));

        assertThrows(BadRequestException.class, () ->
                service.solicitarEliminacaoConta());
    }

    @Test
    void anonimizarUtilizador_DeveAnonimizar() {

        Utente user = criarUtente();

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        service.anonimizarUtilizador(1L);

        assertTrue(user.getNome().contains("Anónimo"));
        assertFalse(user.isActivo());

        verify(utilizadorRepository).save(user);
    }

    @Test
    void anonimizarEEliminarUtilizador_DeveExecutar() {

        Utente user = criarUtente();

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        service.anonimizarEEliminarUtilizador(1L);

        verify(utilizadorRepository, times(1))
                .save(user);
    }

    @Test
    void getUtilizadorAutenticado_DeveRetornar() {

        Utente user = criarUtente();

        mockAuth(user);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(user));

        Utilizador result =
                service.getUtilizadorAutenticado();

        assertEquals(1L, result.getId());
    }

   @Test
    void exportarDadosUtilizador_DeveExportar() {

        Utente user = criarUtente();

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setAssunto("Consulta");
        secretaria.setDescricao("Desc");
        secretaria.setUtente(user);
        secretaria.setMarcacao(marcacao);

        marcacao.setMarcacaoSecretaria(secretaria);

        Documento documento =
                new Documento();

        documento.setMarcacao(marcacao);

        mockAuth(user);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(user));

        when(documentoRepository.findByUtente(user))
                .thenReturn(List.of(documento));

        when(marcacaoRepository.findByUtente(user))
                .thenReturn(List.of(marcacao));

        Map<String, Object> result =
                service.exportarDadosUtilizador();

        assertTrue(result.containsKey("dadosPessoais"));
        assertTrue(result.containsKey("documentos"));
        assertTrue(result.containsKey("marcacoes"));
    }

    @Test
    void exportarDadosUtilizador_DeveIgnorarErros() {

        Utente user = criarUtente();

        mockAuth(user);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(user));

        when(documentoRepository.findByUtente(user))
                .thenThrow(new RuntimeException());

        when(marcacaoRepository.findByUtente(user))
                .thenThrow(new RuntimeException());

        Map<String, Object> result =
                service.exportarDadosUtilizador();

        assertTrue(result.containsKey("documentos"));
        assertTrue(result.containsKey("marcacoes"));
    }

    @Test
    void criarUtilizadorPelaSecretaria_DeveIgnorarErroEmail() {

        CreateUserRequestDTO request =
                new CreateUserRequestDTO();

        request.setNif("123456789");
        request.setName("Nuno");
        request.setEmail("teste@test.com");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utilizadorRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException())
                .when(emailService)
                .sendPassword(
                        any(),
                        any());

        assertDoesNotThrow(() ->
                service.criarUtilizadorPelaSecretaria(request));
    }

    private Utente criarUtente() {

        Utente user =
                new Utente();

        user.setId(1L);
        user.setNome("Nuno");
        user.setEmail("teste@test.com");
        user.setNif("123456789");
        user.setTelefone("999999999");
        user.setActivo(true);

        return user;
    }

    private void mockAuth(Utilizador user) {

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        java.util.Collections.emptyList());

        SecurityContextHolder
                .getContext()
                .setAuthentication(auth);
    }

    private void setField(String fieldName, Object value)
            throws Exception {

        Field field =
                UtilizadorService.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(service, value);
    }
}