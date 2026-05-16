package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.ItemArmazem;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceTest {

    @Mock
    private ArmazemService armazemService;
    @Mock
    private MarcacaoRepository marcacaoRepository;
    @Mock
    private UtenteRepository utenteRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private UtilizadorRepository utilizadorRepository;
    @Mock
    private ItemArmazemRepository itemArmazemRepository;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private MarcacaoValidator marcacaoValidator;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NifValidator nifValidator;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private CalendarioService calendarioService;
    @Mock
    private CryptoUtils cryptoUtils;

    @InjectMocks
    private MarcacaoService marcacaoService;

    @Test
    void criarMarcacaoPresencial_DeveLancarIllegalArgumentException_QuandoValidacaoFalha() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);

        // Mock validator para lançar exceção (simula conflito ou validação falha)
        doThrow(new IllegalArgumentException("Horário já ocupado ou inválido"))
                .when(marcacaoValidator).validarCriacao(request);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> marcacaoService.criarMarcacaoPresencial(request));

        // Ensure validacao was called
        verify(marcacaoValidator).validarCriacao(request);
        // Ensure we didn't proceed to save
        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoRemota_DeveLancarIllegalArgumentException_QuandoValidacaoFalha() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);

        // Mock validator para lançar exceção (simula conflito ou validação falha)
        doThrow(new IllegalArgumentException("Horário já ocupado ou inválido"))
                .when(marcacaoValidator).validarCriacao(request);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> marcacaoService.criarMarcacaoRemota(request));

        // Ensure validacao was called
        verify(marcacaoValidator).validarCriacao(request);
        // Ensure we didn't proceed to save
        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoPresencial_ComSucesso() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);
        request.setCriadoPorId(2L);
        request.setAssunto("Consulta");

        Utente utente = new Utente();
        utente.setId(1L);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);

        // Mock validator para não lançar exceção (validação passa)
        doNothing().when(marcacaoValidator).validarCriacao(request);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao resultado = marcacaoService.criarMarcacaoPresencial(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(now, resultado.getData());
        assertEquals(EventoEstado.AGENDADO, resultado.getEstado());
        assertEquals(funcionario, resultado.getCriadoPor());
        assertNotNull(resultado.getMarcacaoSecretaria());
        assertEquals(AtendimentoTipo.PRESENCIAL, resultado.getMarcacaoSecretaria().getTipoAtendimento());
        assertEquals(utente, resultado.getMarcacaoSecretaria().getUtente());
        assertEquals("Consulta", resultado.getMarcacaoSecretaria().getAssunto());
    }

    @Test
    void criarMarcacaoRemota_ComSucesso() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);
        request.setAssunto("Consulta Remota");

        Utente utente = new Utente();
        utente.setId(1L);

        // Mock validator para não lançar exceção (validação passa)
        doNothing().when(marcacaoValidator).validarCriacao(request);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao resultado = marcacaoService.criarMarcacaoRemota(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(now, resultado.getData());
        assertEquals(EventoEstado.AGENDADO, resultado.getEstado());
        // Na remota, criadoPor pode ser null ou não especificado na lógica atual quando
        // feito por utente
        assertNotNull(resultado.getMarcacaoSecretaria());
        assertEquals(AtendimentoTipo.REMOTO, resultado.getMarcacaoSecretaria().getTipoAtendimento());
        assertEquals(utente, resultado.getMarcacaoSecretaria().getUtente());
        assertEquals("Consulta Remota", resultado.getMarcacaoSecretaria().getAssunto());
    }

    @Test
    void criarMarcacaoPresencial_DeveLancarIllegalArgumentException_QuandoNaoHaUtenteIdNemNif() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");

        doNothing().when(marcacaoValidator).validarCriacao(request);

        assertThrows(IllegalArgumentException.class,
                () -> marcacaoService.criarMarcacaoPresencial(request));

        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoPresencial_DeveLancarEntityNotFoundException_QuandoUtenteIdNaoExiste() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setUtenteId(99L);
        request.setAssunto("Consulta");

        doNothing().when(marcacaoValidator).validarCriacao(request);
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> marcacaoService.criarMarcacaoPresencial(request));

        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoRemota_DeveLancarEntityNotFoundException_QuandoUtenteNaoExiste() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setUtenteId(99L);
        request.setAssunto("Consulta Remota");

        doNothing().when(marcacaoValidator).validarCriacao(request);
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> marcacaoService.criarMarcacaoRemota(request));

        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoPresencial_DeveCriarNovoUtente_QuandoNifNaoExiste() {
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setAssunto("Consulta");
        request.setUtenteNif("100000002");
        request.setUtenteNome("Novo Utente");
        request.setUtenteEmail("novo@teste.com");
        request.setUtenteTelefone("999999999");

        doNothing().when(marcacaoValidator).validarCriacao(request);
        when(cryptoUtils.generateBlindIndex("100000002")).thenReturn("HASHED_NIF");
        when(utenteRepository.findByNifHash("HASHED_NIF")).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(utenteRepository.save(any(Utente.class))).thenAnswer(invocation -> {
            Utente u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Marcacao resultado = marcacaoService.criarMarcacaoPresencial(request);

        assertNotNull(resultado);
        assertEquals("Novo Utente", resultado.getMarcacaoSecretaria().getUtente().getNome());
        assertEquals("100000002", resultado.getMarcacaoSecretaria().getUtente().getNif());
        assertEquals(EventoEstado.AGENDADO, resultado.getEstado());
        assertEquals(15, resultado.getDuration());

        verify(utenteRepository).save(any(Utente.class));
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void criarMarcacaoPresencial_DeveUsarUtenteExistentePorNif_QuandoJaExiste() {
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setAssunto("Consulta");
        request.setUtenteNif("100000002");

        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Utente Existente");
        utente.setNif("100000002");

        doNothing().when(marcacaoValidator).validarCriacao(request);
        doNothing().when(nifValidator).validateRequiredOrThrow("100000002");

        when(cryptoUtils.generateBlindIndex("100000002")).thenReturn("HASHED_NIF");
        when(utenteRepository.findByNifHash("HASHED_NIF")).thenReturn(List.of(utente));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Marcacao resultado = marcacaoService.criarMarcacaoPresencial(request);

        assertNotNull(resultado);
        assertEquals(utente, resultado.getMarcacaoSecretaria().getUtente());
        assertEquals(15, resultado.getDuration());
        verify(utenteRepository, never()).save(any(Utente.class));
    }

    @Test
    void criarReservaTemporaria_DeveCriarReservaSecretariaComSucesso() {
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(data);
        request.setTipoAgenda("SECRETARIA");
        request.setUtenteId(1L);

        Utente utente = new Utente();
        utente.setId(1L);

        when(calendarioService.getCapacidadePorSlot("SECRETARIA")).thenReturn(1);
        when(marcacaoRepository.countByDataAndEstadoInAndTipo(eq(data), anyList(), eq("SECRETARIA"))).thenReturn(0L);
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> {
            Marcacao m = invocation.getArgument(0);
            m.setId(123L);
            return m;
        });

        Long resultado = marcacaoService.criarReservaTemporaria(request);

        assertEquals(123L, resultado);

        ArgumentCaptor<Marcacao> captor = ArgumentCaptor.forClass(Marcacao.class);
        verify(marcacaoRepository).save(captor.capture());

        Marcacao guardada = captor.getValue();
        assertEquals(EventoEstado.EM_PREENCHIMENTO, guardada.getEstado());
        assertEquals(15, guardada.getDuration());
        assertNotNull(guardada.getMarcacaoSecretaria());
        assertEquals("Reserva temporária", guardada.getMarcacaoSecretaria().getAssunto());
    }

    @Test
    void criarReservaTemporaria_DeveCriarReservaBalnearioComSucesso() {
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(data);
        request.setTipoAgenda("BALNEARIO");

        when(calendarioService.getCapacidadePorSlot("BALNEARIO")).thenReturn(2);
        when(marcacaoRepository.countByDataAndEstadoInAndTipo(eq(data), anyList(), eq("BALNEARIO"))).thenReturn(0L);
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> {
            Marcacao m = invocation.getArgument(0);
            m.setId(321L);
            return m;
        });

        Long resultado = marcacaoService.criarReservaTemporaria(request);

        assertEquals(321L, resultado);

        ArgumentCaptor<Marcacao> captor = ArgumentCaptor.forClass(Marcacao.class);
        verify(marcacaoRepository).save(captor.capture());

        Marcacao guardada = captor.getValue();
        assertEquals(EventoEstado.EM_PREENCHIMENTO, guardada.getEstado());
        assertEquals(30, guardada.getDuration());
        assertNotNull(guardada.getMarcacaoBalneario());
        assertEquals("Reserva temporária", guardada.getMarcacaoBalneario().getNomeUtente());
    }

    @Test
    void criarReservaTemporaria_DeveLancarIllegalStateException_QuandoCapacidadeCheia() {
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(data);
        request.setTipoAgenda("SECRETARIA");

        when(calendarioService.getCapacidadePorSlot("SECRETARIA")).thenReturn(1);
        when(marcacaoRepository.countByDataAndEstadoInAndTipo(eq(data), anyList(), eq("SECRETARIA"))).thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> marcacaoService.criarReservaTemporaria(request));

        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void apagarReservaTemporaria_DeveApagarQuandoExiste() {
        when(marcacaoRepository.existsById(5L)).thenReturn(true);

        marcacaoService.apagarReservaTemporaria(5L);

        verify(marcacaoRepository).deleteById(5L);
    }

    @Test
    void apagarReservaTemporaria_NaoDeveApagarQuandoNaoExiste() {
        when(marcacaoRepository.existsById(5L)).thenReturn(false);

        marcacaoService.apagarReservaTemporaria(5L);

        verify(marcacaoRepository, never()).deleteById(anyLong());
    }

    @Test
    void reagendarMarcacao_DeveReagendarComSucesso() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now().plusDays(1));

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        marcacao.setMarcacaoSecretaria(sec);

        LocalDateTime novaData = LocalDateTime.now().plusDays(2);

        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        request.setNovaDataHora(novaData);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(calendarioService.getCapacidadePorSlot("SECRETARIA")).thenReturn(2);
        when(marcacaoRepository.countByDataAndEstadoInAndTipo(eq(novaData), anyList(), eq("SECRETARIA")))
                .thenReturn(0L);
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = marcacaoService.reagendarMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(novaData, resultado.getData());
        verify(marcacaoValidator).validarReagendamento(request, "SECRETARIA");
    }

    @Test
    void limparReservasExpiradas_DeveEliminarMarcacoesUmaAUma() {
        // Arrange
        Marcacao expiradas1 = new Marcacao();
        expiradas1.setId(1L);

        Marcacao expiradas2 = new Marcacao();
        expiradas2.setId(2L);

        when(marcacaoRepository.findByEstadoAndCriadoEmBefore(eq(EventoEstado.EM_PREENCHIMENTO), any()))
                .thenReturn(List.of(expiradas1, expiradas2));

        // Act
        marcacaoService.limparReservasExpiradas();

        // Assert
        verify(marcacaoRepository).findByEstadoAndCriadoEmBefore(eq(EventoEstado.EM_PREENCHIMENTO), any());
        verify(marcacaoRepository).delete(expiradas1);
        verify(marcacaoRepository).delete(expiradas2);
        verify(marcacaoRepository, never()).deleteAllInBatch(any());
    }

    @Test
    void reagendarMarcacao_DeveLancarEntityNotFoundException_QuandoNaoExiste() {
        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        request.setNovaDataHora(LocalDateTime.now().plusDays(2));

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> marcacaoService.reagendarMarcacao(1L, request));
    }

    @Test
    void reagendarMarcacao_DeveLancarIllegalStateException_QuandoHorarioEstaCheio() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now().plusDays(1));

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        marcacao.setMarcacaoSecretaria(sec);

        LocalDateTime novaData = LocalDateTime.now().plusDays(2);

        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        request.setNovaDataHora(novaData);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(calendarioService.getCapacidadePorSlot("SECRETARIA")).thenReturn(1);
        when(marcacaoRepository.countByDataAndEstadoInAndTipo(eq(novaData), anyList(), eq("SECRETARIA")))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> marcacaoService.reagendarMarcacao(1L, request));
    }

    @Test
    void reagendarMarcacao_NaoDeveContarPropriaMarcacaoQuandoMesmaData() {
        LocalDateTime data = LocalDateTime.now().plusDays(2);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(data);

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        marcacao.setMarcacaoSecretaria(sec);

        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        request.setNovaDataHora(data);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(calendarioService.getCapacidadePorSlot("SECRETARIA")).thenReturn(1);
        when(marcacaoRepository.countByDataAndEstadoInAndTipo(eq(data), anyList(), eq("SECRETARIA"))).thenReturn(1L);
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = marcacaoService.reagendarMarcacao(1L, request);

        assertEquals(data, resultado.getData());
    }

    @Test
    void atualizarEstadoMarcacao_DeveLancarEntityNotFoundException_QuandoMarcacaoNaoExiste() {
        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> marcacaoService.atualizarEstadoMarcacao(1L, request));
    }

    @Test
    void atualizarEstadoMarcacao_DeveAtualizarParaConcluidoEDefinirAtendente() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setData(LocalDateTime.now().plusDays(1));

        Utilizador atendente = new Funcionario();
        atendente.setId(10L);
        atendente.setNome("Funcionario X");

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.CONCLUIDO);
        when(request.getFuncionarioId()).thenReturn(10L);
        when(request.getMotivoCancelamento()).thenReturn(null);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(utilizadorRepository.findById(10L)).thenReturn(Optional.of(atendente));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = marcacaoService.atualizarEstadoMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(EventoEstado.CONCLUIDO, marcacao.getEstado());
        assertEquals(atendente, marcacao.getAtendente());
        assertEquals("Funcionario X", resultado.getAtendenteNome());
    }

    @Test
    void atualizarEstadoMarcacao_DeveCancelarEDefinirMotivo() {
        Utente utente = new Utente();
        utente.setId(20L);
        utente.setNome("Utente A");

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        sec.setUtente(utente);
        sec.setAssunto("Consulta");

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setMarcacaoSecretaria(sec);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(99L);

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.CANCELADO);
        when(request.getFuncionarioId()).thenReturn(99L);
        when(request.getMotivoCancelamento()).thenReturn("Motivo teste");

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(utilizadorRepository.findById(99L)).thenReturn(Optional.of(funcionario));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = marcacaoService.atualizarEstadoMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(EventoEstado.CANCELADO, marcacao.getEstado());
        assertEquals("Motivo teste", marcacao.getMotivoCancelamento());
        assertEquals(funcionario, marcacao.getAtendente());

        verify(notificacaoService).notificarCancelamento(eq(20L), eq(marcacao.getData()), eq("Motivo teste"));
    }

    @Test
    void atualizarEstadoMarcacao_DeveNotificarSecretariasQuandoUtenteCancela() {
        Utente utente = new Utente();
        utente.setId(20L);
        utente.setNome("Utente A");

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        sec.setUtente(utente);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setMarcacaoSecretaria(sec);

        Funcionario secretaria1 = new Funcionario();
        secretaria1.setId(100L);

        Funcionario secretaria2 = new Funcionario();
        secretaria2.setId(101L);

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.CANCELADO);
        when(request.getFuncionarioId()).thenReturn(20L); // o próprio utente
        when(request.getMotivoCancelamento()).thenReturn("Já não posso");

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(funcionarioRepository.findByTipo(pt.florinhas.common_data.domain.FuncionarioTipo.SECRETARIA))
                .thenReturn(List.of(secretaria1, secretaria2));

        var resultado = marcacaoService.atualizarEstadoMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(EventoEstado.CANCELADO, marcacao.getEstado());
        assertNull(marcacao.getAtendente());

        verify(notificacaoService).notificarCancelamentoPeloUtente(100L, "Utente A", marcacao.getData());
        verify(notificacaoService).notificarCancelamentoPeloUtente(101L, "Utente A", marcacao.getData());
        verify(notificacaoService, never()).notificarCancelamento(eq(20L), any(), any());
    }

    @Test
    void atualizarEstadoMarcacao_DeveDescontarStockQuandoVaiParaEmProgressoNoBalneario() {
        MarcacaoBalneario bal = new MarcacaoBalneario();

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setMarcacaoBalneario(bal);

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.EM_PROGRESSO);
        when(request.getFuncionarioId()).thenReturn(null);
        when(request.getMotivoCancelamento()).thenReturn(null);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(armazemService.descontarItens(marcacao)).thenReturn(List.of());

        var resultado = marcacaoService.atualizarEstadoMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(EventoEstado.EM_PROGRESSO, marcacao.getEstado());
        verify(armazemService).descontarItens(marcacao);
    }

    @Test
    void atualizarEstadoMarcacao_DeveRestaurarStockQuandoBalnearioVaiDeEmProgressoParaCancelado() {
        MarcacaoBalneario bal = new MarcacaoBalneario();

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.EM_PROGRESSO);
        marcacao.setMarcacaoBalneario(bal);

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.CANCELADO);
        when(request.getFuncionarioId()).thenReturn(null);
        when(request.getMotivoCancelamento()).thenReturn("Motivo");

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = marcacaoService.atualizarEstadoMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(EventoEstado.CANCELADO, marcacao.getEstado());
        verify(armazemService).restaurarItens(marcacao);
    }

    @Test
    void atualizarEstadoMarcacao_DeveRestaurarStockQuandoBalnearioVaiDeEmProgressoParaNaoComparecido() {
        MarcacaoBalneario bal = new MarcacaoBalneario();

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.EM_PROGRESSO);
        marcacao.setMarcacaoBalneario(bal);

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.NAO_COMPARECIDO);
        when(request.getFuncionarioId()).thenReturn(null);
        when(request.getMotivoCancelamento()).thenReturn(null);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = marcacaoService.atualizarEstadoMarcacao(1L, request);

        assertNotNull(resultado);
        assertEquals(EventoEstado.NAO_COMPARECIDO, marcacao.getEstado());
        verify(armazemService).restaurarItens(marcacao);
    }

    @Test
    void atualizarEstadoMarcacao_DeveLancarEntityNotFoundException_QuandoAtendenteNaoExisteAoConcluir() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setEstado(EventoEstado.AGENDADO);

        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        when(request.getNovoEstadoEnum()).thenReturn(EventoEstado.CONCLUIDO);
        when(request.getFuncionarioId()).thenReturn(999L);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(utilizadorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> marcacaoService.atualizarEstadoMarcacao(1L, request));
    }

    @Test
    void criarMarcacaoBalneario_ComReservaExistenteEPreenchimento_DeveAgendarComSucesso() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setReservaId(100L);
        request.setData(LocalDateTime.now().plusDays(1));
        request.setNomeUtente("Utente Balneario");
        request.setProdutosHigiene(true);
        request.setLavagemRoupa(true);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(100L);
        marcacao.setEstado(EventoEstado.EM_PREENCHIMENTO);
        MarcacaoBalneario detalhes = new MarcacaoBalneario();
        marcacao.setMarcacaoBalneario(detalhes);

        when(marcacaoRepository.findById(100L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(i -> i.getArgument(0));

        Marcacao result = marcacaoService.criarMarcacaoBalneario(request);

        assertNotNull(result);
        assertEquals(EventoEstado.AGENDADO, result.getEstado());
        assertTrue(result.getMarcacaoBalneario().getProdutosHigiene());
        assertTrue(result.getMarcacaoBalneario().getLavagemRoupa());
        assertEquals("Utente Balneario", result.getMarcacaoBalneario().getNomeUtente());
    }

    @Test
    void criarMarcacaoBalneario_ComReservaNaoEmPreenchimento_DeveLancarErro() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setReservaId(100L);
        request.setData(LocalDateTime.now().plusDays(1));

        Marcacao marcacao = new Marcacao();
        marcacao.setId(100L);
        marcacao.setEstado(EventoEstado.AGENDADO); // Not EM_PREENCHIMENTO

        when(marcacaoRepository.findById(100L)).thenReturn(Optional.of(marcacao));

        assertThrows(IllegalStateException.class, () -> marcacaoService.criarMarcacaoBalneario(request));
    }

    @Test
    void criarMarcacaoBalneario_SemReserva_DeveCriarNovaMarcacao() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setNomeUtente("Utente Balneario Novo");
        request.setResponsavelId(200L);

        Funcionario responsavel = new Funcionario();
        responsavel.setId(200L);

        when(funcionarioRepository.findById(200L)).thenReturn(Optional.of(responsavel));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(i -> i.getArgument(0));

        Marcacao result = marcacaoService.criarMarcacaoBalneario(request);

        assertNotNull(result);
        assertEquals(EventoEstado.AGENDADO, result.getEstado());
        assertEquals(30, result.getDuration()); // BALNEARIO_DEFAULT_DURATION_MINUTES
        assertEquals(responsavel, result.getCriadoPor());
        assertEquals(responsavel, result.getMarcacaoBalneario().getResponsavel());
    }

    @Test
    void criarMarcacaoBalneario_ItemNaoEncontrado_DeveLancarErro() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setData(LocalDateTime.now().plusDays(1));

        RoupaDTO rDTO = new RoupaDTO();
        rDTO.setItemId(999L);
        rDTO.setCategoria("TOWEL");
        request.setRoupas(List.of(rDTO));

        when(itemArmazemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> marcacaoService.criarMarcacaoBalneario(request));
    }

    @Test
    void criarMarcacaoBalneario_ComRoupasValidas_DeveAdicionarComSucesso() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setData(LocalDateTime.now().plusDays(1));

        RoupaDTO rDTO = new RoupaDTO();
        rDTO.setItemId(10L);
        rDTO.setCategoria("TOWEL");
        rDTO.setTamanho("L");
        rDTO.setQuantidade(2);
        request.setRoupas(List.of(rDTO));

        ItemArmazem item = new ItemArmazem();
        item.setId(10L);

        when(itemArmazemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(i -> i.getArgument(0));

        Marcacao result = marcacaoService.criarMarcacaoBalneario(request);

        assertNotNull(result);
        assertEquals(1, result.getMarcacaoBalneario().getRoupas().size());
        assertEquals("TOWEL", result.getMarcacaoBalneario().getRoupas().get(0).getCategoria());
        assertEquals(2, result.getMarcacaoBalneario().getRoupas().get(0).getQuantidade());
        assertEquals(item, result.getMarcacaoBalneario().getRoupas().get(0).getItem());
    }

    @Test
    void atualizarDetalhesBalneario_MarcacaoNaoEncontrada_DeveLancarErro() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> marcacaoService.atualizarDetalhesBalneario(1L, true, true, List.of()));
    }

    @Test
    void atualizarDetalhesBalneario_SemDetalhesBalneario_DeveLancarErro() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));

        assertThrows(IllegalArgumentException.class,
                () -> marcacaoService.atualizarDetalhesBalneario(1L, true, true, List.of()));
    }

    @Test
    void atualizarDetalhesBalneario_ComSucesso() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        MarcacaoBalneario detalhes = new MarcacaoBalneario();
        marcacao.setMarcacaoBalneario(detalhes);

        RoupaDTO rDTO = new RoupaDTO();
        rDTO.setItemId(10L);
        rDTO.setCategoria("ROBE");
        rDTO.setTamanho("M");
        rDTO.setQuantidade(1);

        ItemArmazem item = new ItemArmazem();
        item.setId(10L);

        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(itemArmazemRepository.findById(10L)).thenReturn(Optional.of(item));

        MarcacaoResponseDTO result = marcacaoService.atualizarDetalhesBalneario(1L, true, true, List.of(rDTO));

        assertNotNull(result);
        assertTrue(detalhes.getProdutosHigiene());
        assertTrue(detalhes.getLavagemRoupa());
        assertEquals(1, detalhes.getRoupas().size());
        assertEquals("ROBE", detalhes.getRoupas().get(0).getCategoria());
        verify(marcacaoRepository).save(marcacao);
    }

    @Test
    void consultarAgenda_DeveRetornarAgendaFiltrada() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);

        Marcacao m = new Marcacao();
        m.setId(1L);
        m.setData(LocalDateTime.now());
        m.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findMarcacoesBetweenDates(eq(inicio), eq(fim), eq("BALNEARIO"))).thenReturn(List.of(m));

        List<MarcacaoResponseDTO> result = marcacaoService.consultarAgenda(inicio, fim, "BALNEARIO");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void procurarAgenda_DeveRetornarResultadosFiltrados() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);

        Marcacao m = new Marcacao();
        m.setId(1L);
        m.setData(LocalDateTime.now());
        m.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findWithFilters(eq(inicio), eq(fim), eq(10L), eq(20L), eq(EventoEstado.AGENDADO)))
                .thenReturn(List.of(m));

        List<MarcacaoResponseDTO> result = marcacaoService.procurarAgenda(inicio, fim, 10L, 20L, EventoEstado.AGENDADO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void criarMarcacaoPresencial_SemTransacaoAtiva_DeveEnviarEmailImediatamente() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");
        request.setUtenteNif("100000002");
        request.setUtenteNome("Novo Utente");
        request.setUtenteEmail("novo@teste.com");
        request.setUtenteTelefone("999999999");

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                TransactionSynchronizationManager.class)) {
            mockedStatic.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(false);

            when(cryptoUtils.generateBlindIndex("100000002")).thenReturn("HASHED_NIF");
            when(utenteRepository.findByNifHash("HASHED_NIF")).thenReturn(List.of());
            when(passwordEncoder.encode(anyString())).thenReturn("HASH");
            when(utenteRepository.save(any(Utente.class))).thenAnswer(invocation -> {
                Utente u = invocation.getArgument(0);
                u.setId(10L);
                return u;
            });
            when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Marcacao resultado = marcacaoService.criarMarcacaoPresencial(request);

            assertNotNull(resultado);
            verify(emailService).sendPassword(eq("novo@teste.com"), anyString());
        }
    }

    @Test
    void criarMarcacaoPresencial_ComTransacaoAtiva_DeveRegistrarSincronizacao() throws Exception {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");
        request.setUtenteNif("100000002");
        request.setUtenteNome("Novo Utente");
        request.setUtenteEmail("novo@teste.com");
        request.setUtenteTelefone("999999999");

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                TransactionSynchronizationManager.class)) {
            mockedStatic.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            doNothing().when(TransactionSynchronizationManager.class);
            TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class));

            when(cryptoUtils.generateBlindIndex("100000002")).thenReturn("HASHED_NIF");
            when(utenteRepository.findByNifHash("HASHED_NIF")).thenReturn(List.of());
            when(passwordEncoder.encode(anyString())).thenReturn("HASH");
            when(utenteRepository.save(any(Utente.class))).thenAnswer(invocation -> {
                Utente u = invocation.getArgument(0);
                u.setId(10L);
                return u;
            });
            when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Marcacao resultado = marcacaoService.criarMarcacaoPresencial(request);

            assertNotNull(resultado);

            verify(emailService, never()).sendPassword(anyString(), anyString());

            ArgumentCaptor<TransactionSynchronization> syncCaptor = ArgumentCaptor
                    .forClass(TransactionSynchronization.class);
            mockedStatic.verify(() -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()),
                    atLeastOnce());

            List<TransactionSynchronization> syncs = syncCaptor.getAllValues();
            assertFalse(syncs.isEmpty());
            for (TransactionSynchronization sync : syncs) {
                sync.afterCommit();
            }

            verify(emailService).sendPassword(eq("novo@teste.com"), anyString());
        }
    }
}
