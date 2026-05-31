package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityNotFoundException;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.ItemArmazem;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.domain.Roupa;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.BalnearioAttendanceStatsDTO;
import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;

class MarcacaoServiceTest {

    private MarcacaoRepository marcacaoRepository;
    private ConfiguracaoAgendaRepository configuracaoAgendaRepository;
    private UtenteRepository utenteRepository;
    private FuncionarioRepository funcionarioRepository;
    private UtilizadorRepository utilizadorRepository;
    private ItemArmazemRepository itemArmazemRepository;
    private NotificacaoService notificacaoService;
    private MarcacaoValidator marcacaoValidator;
    private CryptoUtils cryptoUtils;
    private NifValidator nifValidator;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private ArmazemService armazemService;
    private AuthorizationService authorizationService;
    private AuditLogService auditLogService;
    private FuncionarioService funcionarioService;
    private CalendarioService calendarioService;

    private MarcacaoService service;

    @BeforeEach
    void setUp() {

        marcacaoRepository = mock(MarcacaoRepository.class);
        configuracaoAgendaRepository = mock(ConfiguracaoAgendaRepository.class);
        utenteRepository = mock(UtenteRepository.class);
        funcionarioRepository = mock(FuncionarioRepository.class);
        utilizadorRepository = mock(UtilizadorRepository.class);
        itemArmazemRepository = mock(ItemArmazemRepository.class);
        notificacaoService = mock(NotificacaoService.class);
        marcacaoValidator = mock(MarcacaoValidator.class);
        cryptoUtils = mock(CryptoUtils.class);
        nifValidator = mock(NifValidator.class);
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        armazemService = mock(ArmazemService.class);
        authorizationService = mock(AuthorizationService.class);
        auditLogService = mock(AuditLogService.class);
        funcionarioService = mock(FuncionarioService.class);
        calendarioService = mock(CalendarioService.class);

        service = new MarcacaoService(
                marcacaoRepository,
                configuracaoAgendaRepository,
                utenteRepository,
                funcionarioRepository,
                utilizadorRepository,
                itemArmazemRepository,
                notificacaoService,
                marcacaoValidator,
                cryptoUtils,
                nifValidator,
                emailService,
                passwordEncoder,
                armazemService,
                authorizationService,
                auditLogService,
                funcionarioService,
                calendarioService);
    }

    @Test
    void contarMarcacoesDiarias_DeveRetornarValor() {

        when(marcacaoRepository.countMarcacoesBetweenDates(any(), any()))
                .thenReturn(5L);

        long result = service.contarMarcacoesDiarias(LocalDateTime.now());

        assertEquals(5L, result);
    }

    @Test
    void criarMarcacaoPresencial_DeveCriarComUtenteExistente() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setUtenteId(1L);
        request.setCriadoPorId(2L);
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");
        request.setDescricao("Descrição");

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");
        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);

        mockBloqueioAgenda("SECRETARIA");

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(funcionarioRepository.findById(2L))
                .thenReturn(Optional.of(funcionario));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(10L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoPresencial(request);

        assertEquals(10L, result.getId());
        assertEquals(EventoEstado.AGENDADO, result.getEstado());
        assertEquals(AtendimentoTipo.PRESENCIAL, result.getMarcacaoSecretaria().getTipoAtendimento());
        verify(auditLogService).log(any(), any(), any(), any());
    }

    @Test
    void criarMarcacaoPresencial_DeveCriarNovoUtente() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setUtenteNif("123456789");
        request.setUtenteNome("Nuno");
        request.setUtenteEmail("utente@test.com");
        request.setUtenteTelefone("999999999");
        request.setUtenteDataNasc(LocalDate.now());
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");

        mockBloqueioAgenda("SECRETARIA");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utenteRepository.findByNifHash("hash"))
                .thenReturn(List.of());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(utenteRepository.save(any()))
                .thenReturn(utente);

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(10L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoPresencial(request);

        assertEquals(10L, result.getId());
        verify(utenteRepository).save(any());
    }

    @Test
    void criarMarcacaoPresencial_DeveUsarUtenteExistentePorNif() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setUtenteNif("123456789");
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");

        mockBloqueioAgenda("SECRETARIA");

        when(cryptoUtils.generateBlindIndex("123456789"))
                .thenReturn("hash");

        when(utenteRepository.findByNifHash("hash"))
                .thenReturn(List.of(utente));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(10L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoPresencial(request);

        assertEquals(10L, result.getId());
        verify(utenteRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoPresencial_DeveLancarErroSemUtente() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now());

        mockBloqueioAgenda("SECRETARIA");

        assertThrows(IllegalArgumentException.class, () -> service.criarMarcacaoPresencial(request));
    }

    @Test
    void criarMarcacaoPresencial_DeveLancarErroQuandoUtenteNaoExiste() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setUtenteId(1L);

        mockBloqueioAgenda("SECRETARIA");

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.criarMarcacaoPresencial(request));
    }

    @Test
    void criarMarcacaoRemota_DeveCriar() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setUtenteId(1L);
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");

        mockBloqueioAgenda("SECRETARIA");

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(5L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoRemota(request);

        assertEquals(5L, result.getId());
        assertEquals(AtendimentoTipo.REMOTO, result.getMarcacaoSecretaria().getTipoAtendimento());
    }

    @Test
    void criarMarcacaoRemota_DeveLancarErroQuandoUtenteNaoExiste() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setUtenteId(1L);

        mockBloqueioAgenda("SECRETARIA");

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.criarMarcacaoRemota(request));
    }

    @Test
    void criarMarcacaoBalneario_DeveCriar() {

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setNomeUtente("Nuno");
        request.setProdutosHigiene(true);
        request.setLavagemRoupa(false);
        request.setObservacoes("Obs");
        request.setData(LocalDateTime.now().plusDays(1));

        mockBloqueioAgenda("BALNEARIO");

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(1L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoBalneario(request);

        assertEquals(1L, result.getId());
        assertEquals(EventoEstado.AGENDADO, result.getEstado());
        assertEquals("Nuno", result.getMarcacaoBalneario().getNomeUtente());
    }

    @Test
    void criarMarcacaoBalneario_DeveCriarComResponsavel() {

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setResponsavelId(2L);
        request.setNomeUtente("Nuno");
        request.setData(LocalDateTime.now().plusDays(1));

        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);

        mockBloqueioAgenda("BALNEARIO");

        when(funcionarioRepository.findById(2L))
                .thenReturn(Optional.of(funcionario));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(1L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoBalneario(request);

        assertEquals(funcionario, result.getCriadoPor());
        assertEquals(funcionario, result.getMarcacaoBalneario().getResponsavel());
    }

    @Test
    void criarMarcacaoBalneario_DeveCriarComRoupaEItem() {

        ItemArmazem item = new ItemArmazem();
        item.setId(3L);

        RoupaDTO roupa = new RoupaDTO();
        roupa.setCategoria("Calças");
        roupa.setTamanho("L");
        roupa.setQuantidade(2);
        roupa.setItemId(3L);

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setNomeUtente("Nuno");
        request.setData(LocalDateTime.now().plusDays(1));
        request.setRoupas(List.of(roupa));

        mockBloqueioAgenda("BALNEARIO");

        when(itemArmazemRepository.findAllById(List.of(3L)))
                .thenReturn(List.of(item));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(1L);
                    return marcacao;
                });

        Marcacao result = service.criarMarcacaoBalneario(request);

        assertEquals(1, result.getMarcacaoBalneario().getRoupas().size());
        assertEquals(item, result.getMarcacaoBalneario().getRoupas().get(0).getItem());
    }

    @Test
    void criarMarcacaoBalneario_DeveLancarErroItemNaoExiste() {

        RoupaDTO roupa = new RoupaDTO();
        roupa.setItemId(3L);

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setNomeUtente("Nuno");
        request.setData(LocalDateTime.now().plusDays(1));
        request.setRoupas(List.of(roupa));

        mockBloqueioAgenda("BALNEARIO");

        when(itemArmazemRepository.findAllById(List.of(3L)))
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.criarMarcacaoBalneario(request));
    }

    @Test
    void criarMarcacaoBalneario_DeveUsarReservaTemporaria() {

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setReservaId(1L);
        request.setNomeUtente("Nuno");
        request.setData(LocalDateTime.now().plusDays(1));

        Marcacao reserva = new Marcacao();
        reserva.setId(1L);
        reserva.setEstado(EventoEstado.EM_PREENCHIMENTO);

        mockBloqueioAgenda("BALNEARIO");

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Marcacao result = service.criarMarcacaoBalneario(request);

        assertEquals(EventoEstado.AGENDADO, result.getEstado());
        assertEquals("Nuno", result.getMarcacaoBalneario().getNomeUtente());
    }

    @Test
    void criarMarcacaoBalneario_DeveLancarErroReservaInvalida() {

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setReservaId(1L);

        Marcacao marcacao = new Marcacao();
        marcacao.setEstado(EventoEstado.CONCLUIDO);

        mockBloqueioAgenda("BALNEARIO");

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        assertThrows(IllegalStateException.class, () -> service.criarMarcacaoBalneario(request));
    }

    @Test
    void atualizarDetalhesBalneario_DeveAtualizar() {

        Marcacao marcacao = criarMarcacaoBalneario(1L);

        RoupaDTO roupa = new RoupaDTO();
        roupa.setCategoria("Meias");
        roupa.setQuantidade(2);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        MarcacaoResponseDTO result = service.atualizarDetalhesBalneario(1L, true, false, List.of(roupa));

        assertEquals(true, result.getMarcacaoBalneario().getProdutosHigiene());
        assertEquals(1, result.getMarcacaoBalneario().getRoupas().size());
    }

    @Test
    void atualizarDetalhesBalneario_DeveLancarErroSemDetalhes() {

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(new Marcacao()));

        assertThrows(IllegalArgumentException.class, () -> service.atualizarDetalhesBalneario(1L, true, false, List.of()));
    }

    @Test
    void consultarAgenda_DeveRetornarLista() {

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), any()))
                .thenReturn(List.of(new Marcacao()));

        List<MarcacaoResponseDTO> result = service.consultarAgenda(null, null, "SECRETARIA");

        assertEquals(1, result.size());
    }

    @Test
    void procurarAgenda_DeveRetornarLista() {

        when(marcacaoRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new Marcacao()));

        List<MarcacaoResponseDTO> result = service.procurarAgenda(null, null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void atualizarEstadoMarcacao_DeveCancelarPelaSecretaria() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CANCELADO");
        request.setFuncionarioId(2L);
        request.setMotivoCancelamento("Motivo");

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");
        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setNome("Secretaria");

        Marcacao marcacao = criarMarcacaoSecretaria(10L, utente);
        marcacao.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findById(10L))
                .thenReturn(Optional.of(marcacao));

        when(utilizadorRepository.findById(2L))
                .thenReturn(Optional.of(funcionario));

        when(funcionarioRepository.findById(2L))
                .thenReturn(Optional.of(funcionario));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarcacaoResponseDTO result = service.atualizarEstadoMarcacao(10L, request);

        assertEquals(EventoEstado.CANCELADO, result.getEstado());
        assertEquals("Motivo", result.getMotivoCancelamento());
        verify(notificacaoService).notificarCancelamento(1L, marcacao.getData(), "Motivo");
        verify(emailService).sendAppointmentCancelled("utente@test.com", "Secretaria", marcacao.getData(), "Consulta", "Motivo");
    }

    @Test
    void atualizarEstadoMarcacao_DeveCancelarPeloUtenteENotificarSecretarias() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CANCELADO");
        request.setFuncionarioId(1L);

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");

        Funcionario secretaria = new Funcionario();
        secretaria.setId(2L);

        Marcacao marcacao = criarMarcacaoSecretaria(10L, utente);
        marcacao.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findById(10L))
                .thenReturn(Optional.of(marcacao));

        when(funcionarioService.listarSecretarias())
                .thenReturn(List.of(secretaria));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarcacaoResponseDTO result = service.atualizarEstadoMarcacao(10L, request);

        assertEquals(EventoEstado.CANCELADO, result.getEstado());
        verify(notificacaoService).notificarCancelamentoPeloUtente(2L, "Nuno", marcacao.getData());
    }

    @Test
    void atualizarEstadoMarcacao_DeveConcluirEDefinirAtendente() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CONCLUIDO");
        request.setFuncionarioId(2L);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setNome("Técnico");

        Marcacao marcacao = new Marcacao();
        marcacao.setEstado(EventoEstado.EM_PROGRESSO);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(utilizadorRepository.findById(2L))
                .thenReturn(Optional.of(funcionario));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarcacaoResponseDTO result = service.atualizarEstadoMarcacao(1L, request);

        assertEquals(EventoEstado.CONCLUIDO, result.getEstado());
        assertEquals("Técnico", result.getAtendenteNome());
    }

    @Test
    void atualizarEstadoMarcacao_DeveDescontarStock() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("EM_PROGRESSO");

        Marcacao marcacao = criarMarcacaoBalneario(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(armazemService.descontarItens(marcacao))
                .thenReturn(List.of());

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.atualizarEstadoMarcacao(1L, request);

        verify(armazemService).descontarItens(marcacao);
    }

    @Test
    void atualizarEstadoMarcacao_DeveRestaurarStock() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CANCELADO");

        Marcacao marcacao = criarMarcacaoBalneario(1L);
        marcacao.setEstado(EventoEstado.EM_PROGRESSO);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.atualizarEstadoMarcacao(1L, request);

        verify(armazemService).restaurarItens(marcacao);
    }

    @Test
    void atualizarEstadoMarcacao_DeveLancarErroQuandoNaoExiste() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CANCELADO");

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.atualizarEstadoMarcacao(1L, request));
    }

    @Test
    void consultarMarcacoesPassadas_DeveRetornarLista() {

        when(marcacaoRepository.findMarcacoesPassadas(any(), any(), any(), any()))
                .thenReturn(List.of(new Marcacao()));

        List<MarcacaoResponseDTO> result = service.consultarMarcacoesPassadas(null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void consultarMarcacoesPassadasPaginated_DeveRetornarPagina() {

        PageRequest pageable = PageRequest.of(0, 10);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);

        when(marcacaoRepository.findMarcacoesPassadasPaginatedIds(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(1L), pageable, 1));

        when(marcacaoRepository.findAllByIdWithDetails(List.of(1L)))
                .thenReturn(List.of(marcacao));

        assertEquals(1, service.consultarMarcacoesPassadasPaginated(null, null, null, null, null, null, pageable).getTotalElements());
    }

    @Test
    void notificarDocumentosInvalidos_DeveNotificar() {

        NotificarDocumentosRequest request = new NotificarDocumentosRequest();
        request.setObservacoes("Falta documento");

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");
        Marcacao marcacao = criarMarcacaoSecretaria(10L, utente);

        when(marcacaoRepository.findById(10L))
                .thenReturn(Optional.of(marcacao));

        MarcacaoResponseDTO result = service.notificarDocumentosInvalidos(10L, request);

        assertEquals(10L, result.getId());
        verify(notificacaoService).notificarDocumentosInvalidos(1L, "Falta documento");
    }

    @Test
    void notificarDocumentosInvalidos_DeveLancarErroSemUtente() {

        when(marcacaoRepository.findById(10L))
                .thenReturn(Optional.of(new Marcacao()));

        assertThrows(IllegalStateException.class, () -> service.notificarDocumentosInvalidos(10L, new NotificarDocumentosRequest()));
    }

    @Test
    void consultarMarcacoesUtente_DeveRetornarLista() {

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(marcacaoRepository.findByUtente(utente))
                .thenReturn(List.of(new Marcacao()));

        assertEquals(1, service.consultarMarcacoesUtente(1L).size());
    }

    @Test
    void consultarMarcacoesUtente_DeveLancarErro() {

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.consultarMarcacoesUtente(1L));
    }

    @Test
    void consultarMarcacoesBloqueadas_DeveFiltrarDoProprioUtente() {

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");
        Marcacao marcacao = criarMarcacaoSecretaria(10L, utente);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), anyString()))
                .thenReturn(List.of(marcacao));

        List<Map<String, Object>> result = service.consultarMarcacoesBloqueadas(1L);

        assertEquals(0, result.size());
    }

    @Test
    void consultarMarcacoesBloqueadas_DeveRetornarMarcacaoDeOutroUtente() {

        Utente utente = criarUtente(2L, "Maria", "maria@test.com");
        Marcacao marcacao = criarMarcacaoSecretaria(10L, utente);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), anyString()))
                .thenReturn(List.of(marcacao));

        List<Map<String, Object>> result = service.consultarMarcacoesBloqueadas(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).get("id"));
    }

    @Test
    void consultarMarcacoesFuncionario_DeveRetornarLista() {

        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(marcacaoRepository.findByCriadoPor(funcionario))
                .thenReturn(List.of(new Marcacao()));

        assertEquals(1, service.consultarMarcacoesFuncionario(1L).size());
    }

    @Test
    void obterMarcacaoDTO_DeveRetornarDto() {

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        assertEquals(1L, service.obterMarcacaoDTO(1L).getId());
    }

    @Test
    void listarTodasMarcacoesPaginated_DeveRetornarPaginaVazia() {

        PageRequest pageable = PageRequest.of(0, 10);

        when(marcacaoRepository.findAllIdsPaginated(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertEquals(0, service.listarTodasMarcacoesPaginated(pageable).getTotalElements());
    }

    @Test
    void listarTodasMarcacoesPaginated_DeveRetornarPagina() {

        PageRequest pageable = PageRequest.of(0, 10);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);

        when(marcacaoRepository.findAllIdsPaginated(pageable))
                .thenReturn(new PageImpl<>(List.of(1L), pageable, 1));

        when(marcacaoRepository.findAllByIdWithDetails(List.of(1L)))
                .thenReturn(List.of(marcacao));

        assertEquals(1, service.listarTodasMarcacoesPaginated(pageable).getTotalElements());
    }

    @Test
    void criarReservaTemporaria_DeveCriarSecretaria() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setTipoAgenda("SECRETARIA");
        request.setData(LocalDateTime.now());
        request.setUtenteId(1L);

        Utente utente = criarUtente(1L, "Nuno", "utente@test.com");

        mockBloqueioAgenda("SECRETARIA");

        when(calendarioService.getCapacidadePorSlot("SECRETARIA"))
                .thenReturn(5);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(0L);

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(10L);
                    return marcacao;
                });

        assertEquals(10L, service.criarReservaTemporaria(request));
    }

    @Test
    void criarReservaTemporaria_DeveCriarBalneario() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setTipoAgenda("BALNEARIO");
        request.setData(LocalDateTime.now());

        mockBloqueioAgenda("BALNEARIO");

        when(calendarioService.getCapacidadePorSlot("BALNEARIO"))
                .thenReturn(5);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(0L);

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> {
                    Marcacao marcacao = invocation.getArgument(0);
                    marcacao.setId(10L);
                    return marcacao;
                });

        assertEquals(10L, service.criarReservaTemporaria(request));
    }

    @Test
    void criarReservaTemporaria_DeveLancarErroCapacidade() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setTipoAgenda("SECRETARIA");
        request.setData(LocalDateTime.now());

        mockBloqueioAgenda("SECRETARIA");

        when(calendarioService.getCapacidadePorSlot("SECRETARIA"))
                .thenReturn(1);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.criarReservaTemporaria(request));
    }

    @Test
    void apagarReservaTemporaria_DeveApagar() {

        when(marcacaoRepository.existsById(1L))
                .thenReturn(true);

        service.apagarReservaTemporaria(1L);

        verify(marcacaoRepository).deleteById(1L);
    }

    @Test
    void apagarReservaTemporaria_NaoDeveApagarQuandoNaoExiste() {

        when(marcacaoRepository.existsById(1L))
                .thenReturn(false);

        service.apagarReservaTemporaria(1L);

        verify(marcacaoRepository, never()).deleteById(1L);
    }

    @Test
    void reagendarMarcacao_DeveReagendar() {

        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        request.setNovaDataHora(LocalDateTime.now().plusDays(2));

        Marcacao marcacao = criarMarcacaoSecretaria(1L, criarUtente(1L, "Nuno", "utente@test.com"));

        mockBloqueioAgenda("SECRETARIA");

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(calendarioService.getCapacidadePorSlot("SECRETARIA"))
                .thenReturn(5);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(0L);

        when(marcacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarcacaoResponseDTO result = service.reagendarMarcacao(1L, request);

        assertEquals(1L, result.getId());
        assertEquals(request.getNovaDataHora(), result.getData());
    }

    @Test
    void reagendarMarcacao_DeveLancarErroCapacidade() {

        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        request.setNovaDataHora(LocalDateTime.now().plusDays(2));

        Marcacao marcacao = criarMarcacaoSecretaria(1L, criarUtente(1L, "Nuno", "utente@test.com"));

        mockBloqueioAgenda("SECRETARIA");

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(calendarioService.getCapacidadePorSlot("SECRETARIA"))
                .thenReturn(1);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.reagendarMarcacao(1L, request));
    }

    @Test
    void limparReservasExpiradas_DeveEliminar() {

        Marcacao marcacao = new Marcacao();

        when(marcacaoRepository.findByEstadoAndCriadoEmBefore(any(), any()))
                .thenReturn(List.of(marcacao));

        service.limparReservasExpiradas();

        verify(marcacaoRepository).delete(marcacao);
    }

    @Test
    void invalidarMarcacoesExpiradas_DeveExecutarQueries() {

        when(marcacaoRepository.atualizarMarcacoesPorEstadoAntigas(any(), any(), any()))
                .thenReturn(1);

        when(marcacaoRepository.invalidarMarcacoesAntigas(any(), any(), any()))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.invalidarMarcacoesExpiradas());
    }

    @Test
    void obterEstatisticasFrequenciaBalneario_DeveRetornarStats() {

        when(marcacaoRepository.countBalnearioAttendance(any(), any()))
                .thenReturn(3L);

        when(marcacaoRepository.countTotalBalnearioAttendance(any(), any()))
                .thenReturn(5L);

        when(marcacaoRepository.countBalnearioFaltas(any(), any()))
                .thenReturn(1L);

        when(marcacaoRepository.countBalnearioAgendadas(any(), any()))
                .thenReturn(1L);

        when(marcacaoRepository.findAttendanceByDay(any(), any()))
                .thenReturn(List.<Object[]>of(
                        new Object[] { "2026-01-01", 3L }));

        when(marcacaoRepository.findAttendanceByHour(any(), any()))
                .thenReturn(List.<Object[]>of(
                        new Object[] { 10, 2L }));

        BalnearioAttendanceStatsDTO result = service.obterEstatisticasFrequenciaBalneario("MES");

        assertEquals(3L, result.getTotalPresencas());
        assertEquals(5L, result.getTotalMarcacoes());
        assertEquals(1, result.getPresencasPorDia().size());
        assertEquals(2L, result.getPresencasPorHora().get(10));
    }

    private void mockBloqueioAgenda(String tipo) {

        ConfiguracaoAgenda config = new ConfiguracaoAgenda();

        when(configuracaoAgendaRepository.findByTipoWithWriteLock(tipo))
                .thenReturn(Optional.of(config));
    }

    private Utente criarUtente(Long id, String nome, String email) {

        Utente utente = new Utente();

        utente.setId(id);
        utente.setNome(nome);
        utente.setEmail(email);
        utente.setNif("123456789");

        return utente;
    }

    private Marcacao criarMarcacaoSecretaria(Long id, Utente utente) {

        Marcacao marcacao = new Marcacao();

        marcacao.setId(id);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setEstado(EventoEstado.AGENDADO);

        MarcacaoSecretaria secretaria = new MarcacaoSecretaria();

        secretaria.setAssunto("Consulta");
        secretaria.setDescricao("Descrição");
        secretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);
        secretaria.setUtente(utente);
        secretaria.setMarcacao(marcacao);

        marcacao.setMarcacaoSecretaria(secretaria);

        return marcacao;
    }

    private Marcacao criarMarcacaoBalneario(Long id) {

        Marcacao marcacao = new Marcacao();

        marcacao.setId(id);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setEstado(EventoEstado.AGENDADO);

        MarcacaoBalneario balneario = new MarcacaoBalneario();

        balneario.setNomeUtente("Nuno");
        balneario.setProdutosHigiene(false);
        balneario.setLavagemRoupa(false);
        balneario.setMarcacao(marcacao);

        Roupa roupa = new Roupa();

        roupa.setCategoria("Meias");
        roupa.setQuantidade(1);

        balneario.addRoupa(roupa);

        marcacao.setMarcacaoBalneario(balneario);

        return marcacao;
    }
}