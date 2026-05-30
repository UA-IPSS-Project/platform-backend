package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

class CalendarioServiceTest {

    private BloqueioRepository bloqueioRepository;
    private MarcacaoRepository marcacaoRepository;
    private ConfiguracaoAgendaRepository configuracaoAgendaRepository;
    private NotificacaoService notificacaoService;
    private AuditLogService auditLogService;

    private CalendarioService service;

    @BeforeEach
    void setUp() throws Exception {

        bloqueioRepository = mock(BloqueioRepository.class);
        marcacaoRepository = mock(MarcacaoRepository.class);
        configuracaoAgendaRepository = mock(ConfiguracaoAgendaRepository.class);
        notificacaoService = mock(NotificacaoService.class);
        auditLogService = mock(AuditLogService.class);

        service = new CalendarioService(
                        bloqueioRepository,
                        marcacaoRepository,
                        configuracaoAgendaRepository,
                        notificacaoService,
                        auditLogService);

        setField("feriadosCache", new ConcurrentHashMap<>());
        setField("capacidadeCache", new ConcurrentHashMap<>());
    }

    @Test
    void getCapacidadePorSlot_DeveRetornarDefault() {

        when(configuracaoAgendaRepository.findByTipo("SECRETARIA"))
                .thenReturn(java.util.Optional.empty());

        int result = service.getCapacidadePorSlot("SECRETARIA");

        assertEquals(1, result);
    }

    @Test
    void atualizarCapacidadePorSlot_DeveAtualizar() {

        ConfiguracaoAgenda configuracao = new ConfiguracaoAgenda();

        configuracao.setTipo("SECRETARIA");

        when(configuracaoAgendaRepository.findByTipo("SECRETARIA"))
                .thenReturn(java.util.Optional.of(configuracao));

        when(configuracaoAgendaRepository.save(any()))
                .thenReturn(configuracao);

        assertEquals(
                5,
                service.atualizarCapacidadePorSlot(
                        "SECRETARIA",
                        5)
                        .getCapacidadePorSlot());
    }

    @Test
    void atualizarCapacidadePorSlot_DeveLancarErro() {

        assertThrows(
                BadRequestException.class,
                () -> service.atualizarCapacidadePorSlot(
                        "SECRETARIA",
                        0));
    }

    @Test
    void isSlotBloqueado_DeveRetornarTrueQuandoCapacidadeCheia() {

        LocalDate data = LocalDate.now().plusDays(1);

        when(bloqueioRepository.findByDataAndTipo(any(), any()))
                .thenReturn(List.of());

        when(configuracaoAgendaRepository.findByTipo("SECRETARIA"))
                .thenReturn(java.util.Optional.empty());

        when(marcacaoRepository.countByDataAndTipo(any(), any()))
                .thenReturn(1L);

        boolean result =
                service.isSlotBloqueado(
                        data,
                        LocalTime.of(10, 0),
                        "SECRETARIA");

        assertEquals(true, result);
    }

    @Test
    void bloquearHorario_DeveCriarBloqueio() {

        LocalDate data = LocalDate.now().plusDays(1);

        when(bloqueioRepository.countConflictingWithLockByTipo(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(0L);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                any()))
                .thenReturn(List.of());

        when(bloqueioRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        BloqueioAgenda result =
                service.bloquearHorario(
                        data,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        mock(Utilizador.class),
                        "SECRETARIA");

        assertEquals("Teste", result.getMotivo());
    }

    @Test
    void bloquearHorario_DeveLancarErroDataPassada() {

        assertThrows(
                BadRequestException.class,
                () -> service.bloquearHorario(
                        LocalDate.now().minusDays(1),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        mock(Utilizador.class),
                        "SECRETARIA"));
    }

    @Test
    void removerBloqueio_DeveRemover() {

        service.removerBloqueio(1L);

        verify(bloqueioRepository)
                .deleteById(1L);
    }

    @Test
    void getTodosBloqueios_DeveRetornarLista() {

        when(bloqueioRepository.findAll())
                .thenReturn(List.of(new BloqueioAgenda()));

        assertEquals(1, service.getTodosBloqueios(null).size());
    }

    private void setField(String field, Object value) throws Exception {

        Field f = CalendarioService.class.getDeclaredField(field);

        f.setAccessible(true);

        f.set(service, value);
    }
}