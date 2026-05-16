package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

class CalendarioServiceTest {

    private BloqueioRepository bloqueioRepository;

    private MarcacaoRepository marcacaoRepository;

    private ConfiguracaoAgendaRepository configuracaoAgendaRepository;

    private CalendarioService calendarioService;

    @BeforeEach
    void setUp() {

        bloqueioRepository =
                org.mockito.Mockito.mock(
                        BloqueioRepository.class);

        marcacaoRepository =
                org.mockito.Mockito.mock(
                        MarcacaoRepository.class);

        configuracaoAgendaRepository =
                org.mockito.Mockito.mock(
                        ConfiguracaoAgendaRepository.class);

        calendarioService =
                new CalendarioService(
                        bloqueioRepository,
                        marcacaoRepository,
                        configuracaoAgendaRepository);
    }

    @Test
    void getCapacidadePorSlot_DeveRetornarDefaultSecretaria() {

        int result =
                calendarioService.getCapacidadePorSlot(
                        "SECRETARIA");

        assertEquals(
                1,
                result);
    }

    @Test
    void getCapacidadePorSlot_DeveRetornarDefaultBalneario() {

        int result =
                calendarioService.getCapacidadePorSlot(
                        "BALNEARIO");

        assertEquals(
                2,
                result);
    }

    @Test
    void getCapacidadePorSlot_DeveRetornarConfiguracaoBD() {

        ConfiguracaoAgenda config =
                new ConfiguracaoAgenda();

        config.setTipo("SECRETARIA");

        config.setCapacidadePorSlot(5);

        when(configuracaoAgendaRepository.findByTipo(
                "SECRETARIA"))
                .thenReturn(Optional.of(config));

        int result =
                calendarioService.getCapacidadePorSlot(
                        "SECRETARIA");

        assertEquals(
                5,
                result);
    }

    @Test
    void atualizarCapacidadePorSlot_DeveAtualizar() {

        ConfiguracaoAgenda config =
                new ConfiguracaoAgenda();

        config.setTipo("SECRETARIA");

        config.setCapacidadePorSlot(3);

        when(configuracaoAgendaRepository.findByTipo(
                "SECRETARIA"))
                .thenReturn(Optional.of(config));

        when(configuracaoAgendaRepository.save(
                any()))
                .thenAnswer(i -> i.getArgument(0));

        ConfiguracaoSlotDTO dto =
                calendarioService
                        .atualizarCapacidadePorSlot(
                                "SECRETARIA",
                                10);

        assertEquals(
                "SECRETARIA",
                dto.getTipo());

        assertEquals(
                10,
                dto.getCapacidadePorSlot());
    }

    @Test
    void atualizarCapacidadePorSlot_DeveCriarNovaConfig() {

        when(configuracaoAgendaRepository.findByTipo(
                "BALNEARIO"))
                .thenReturn(Optional.empty());

        when(configuracaoAgendaRepository.save(
                any()))
                .thenAnswer(i -> i.getArgument(0));

        ConfiguracaoSlotDTO dto =
                calendarioService
                        .atualizarCapacidadePorSlot(
                                "BALNEARIO",
                                7);

        assertEquals(
                "BALNEARIO",
                dto.getTipo());

        assertEquals(
                7,
                dto.getCapacidadePorSlot());
    }

    @Test
    void atualizarCapacidadePorSlot_DeveLancarExcecaoCapacidadeInvalida() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService
                        .atualizarCapacidadePorSlot(
                                "SECRETARIA",
                                0));
    }

    @Test
    void atualizarCapacidadePorSlot_DeveLancarExcecaoTipoInvalido() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService
                        .atualizarCapacidadePorSlot(
                                "INVALIDO",
                                2));
    }

    @Test
    void listarConfiguracoesSlot_DeveRetornarLista() {

        List<ConfiguracaoSlotDTO> result =
                calendarioService
                        .listarConfiguracoesSlot();

        assertEquals(
                2,
                result.size());
    }

    @Test
    void isSlotBloqueado_DeveRetornarTrueQuandoFimSemana() {

        boolean result =
                calendarioService.isSlotBloqueado(
                        LocalDate.of(2026, 5, 17),
                        LocalTime.of(10, 0),
                        "SECRETARIA");

        assertTrue(result);
    }

    @Test
    void isSlotBloqueado_DeveRetornarFalseSemBloqueios() {

        LocalDate data =
                LocalDate.of(2026, 5, 18);

        when(bloqueioRepository.findByDataAndTipo(
                any(),
                anyString()))
                .thenReturn(List.of());

        when(marcacaoRepository.countByDataAndTipo(
                any(),
                anyString()))
                .thenReturn(0L);

        boolean result =
                calendarioService.isSlotBloqueado(
                        data,
                        LocalTime.of(10, 0),
                        "SECRETARIA");

        assertFalse(result);
    }

    @Test
    void isSlotBloqueado_DeveRetornarTrueQuandoCapacidadeExcedida() {

        LocalDate data =
                LocalDate.of(2026, 5, 18);

        when(bloqueioRepository.findByDataAndTipo(
                any(),
                anyString()))
                .thenReturn(List.of());

        when(marcacaoRepository.countByDataAndTipo(
                any(),
                anyString()))
                .thenReturn(1L);

        boolean result =
                calendarioService.isSlotBloqueado(
                        data,
                        LocalTime.of(10, 0),
                        "SECRETARIA");

        assertTrue(result);
    }

    @Test
    void bloquearHorario_DeveCriarBloqueio() {

        LocalDate data =
                LocalDate.of(2026, 5, 18);

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
                calendarioService.bloquearHorario(
                        data,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        new Utilizador(),
                        "SECRETARIA");

        assertNotNull(result);

        assertEquals(
                "Teste",
                result.getMotivo());
    }

    @Test
    void bloquearHorario_DeveLancarExcecaoDataPassado() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        LocalDate.now().minusDays(1),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        new Utilizador(),
                        "SECRETARIA"));
    }

    @Test
    void bloquearHorario_DeveLancarExcecaoHorarioInvalido() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        LocalDate.of(2026, 5, 18),
                        LocalTime.of(8, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        new Utilizador(),
                        "SECRETARIA"));
    }

    @Test
    void bloquearHorario_DeveLancarExcecaoOrdemHoras() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        LocalDate.of(2026, 5, 18),
                        LocalTime.of(11, 0),
                        LocalTime.of(10, 0),
                        "Teste",
                        new Utilizador(),
                        "SECRETARIA"));
    }

    @Test
    void bloquearHorario_DeveLancarExcecaoConflito() {

        when(bloqueioRepository.countConflictingWithLockByTipo(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(1L);

        assertThrows(
                BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        LocalDate.of(2026, 5, 18),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        new Utilizador(),
                        "SECRETARIA"));
    }

    @Test
    void bloquearHorario_DeveLancarExcecaoMarcacoesAtivas() {

        Marcacao marcacao =
                new Marcacao();

        marcacao.setEstado(
                EventoEstado.AGENDADO);

        marcacao.setData(
                LocalDateTime.now());

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
                .thenReturn(List.of(marcacao));

        assertThrows(
                BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        LocalDate.of(2026, 5, 18),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Teste",
                        new Utilizador(),
                        "SECRETARIA"));
    }

    @Test
    void removerBloqueio_DeveExecutarSemErro() {

        assertDoesNotThrow(
                () -> calendarioService.removerBloqueio(
                        1L));
    }

    @Test
    void getBloqueiosDoMes_DeveRetornarPorTipo() {

        when(bloqueioRepository.findByDataBetweenAndTipo(
                any(),
                any(),
                anyString()))
                .thenReturn(List.of());

        List<BloqueioAgenda> result =
                calendarioService.getBloqueiosDoMes(
                        2026,
                        5,
                        "SECRETARIA");

        assertNotNull(result);
    }

    @Test
    void getTodosBloqueios_DeveRetornarTodos() {

        when(bloqueioRepository.findAll())
                .thenReturn(List.of());

        List<BloqueioAgenda> result =
                calendarioService.getTodosBloqueios(
                        null);

        assertNotNull(result);
    }
}