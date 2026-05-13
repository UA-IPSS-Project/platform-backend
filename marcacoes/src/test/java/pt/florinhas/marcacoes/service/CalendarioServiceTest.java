package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void setup() {

        bloqueioRepository =
                mock(BloqueioRepository.class);

        marcacaoRepository =
                mock(MarcacaoRepository.class);

        configuracaoAgendaRepository =
                mock(ConfiguracaoAgendaRepository.class);

        calendarioService =
                new CalendarioService(
                        bloqueioRepository,
                        marcacaoRepository,
                        configuracaoAgendaRepository
                );
    }

    @Test
    void getCapacidadePorSlot_DeveUsarConfiguracao() {

        ConfiguracaoAgenda cfg =
                new ConfiguracaoAgenda();

        cfg.setTipo("SECRETARIA");
        cfg.setCapacidadePorSlot(5);

        when(configuracaoAgendaRepository
                .findByTipo("SECRETARIA"))
                .thenReturn(Optional.of(cfg));

        int resultado =
                calendarioService
                        .getCapacidadePorSlot("SECRETARIA");

        assertEquals(5, resultado);
    }

    @Test
    void getCapacidadePorSlot_DeveUsarDefault() {

        when(configuracaoAgendaRepository
                .findByTipo("SECRETARIA"))
                .thenReturn(Optional.empty());

        int resultado =
                calendarioService
                        .getCapacidadePorSlot("SECRETARIA");

        assertEquals(1, resultado);
    }

    @Test
    void atualizarCapacidadePorSlot_DeveAtualizar() {

        ConfiguracaoAgenda cfg =
                new ConfiguracaoAgenda();

        cfg.setTipo("SECRETARIA");

        when(configuracaoAgendaRepository
                .findByTipo("SECRETARIA"))
                .thenReturn(Optional.of(cfg));

        when(configuracaoAgendaRepository
                .save(any()))
                .thenAnswer(i -> i.getArgument(0));

        ConfiguracaoSlotDTO dto =
                calendarioService
                        .atualizarCapacidadePorSlot(
                                "SECRETARIA",
                                10
                        );

        assertEquals(10,
                dto.getCapacidadePorSlot());
    }

    @Test
    void atualizarCapacidadePorSlot_DeveLancarErro() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService
                        .atualizarCapacidadePorSlot(
                                "SECRETARIA",
                                0
                        )
        );
    }

    @Test
    void isSlotBloqueado_DeveRetornarTrueQuandoCapacidadeAtingida() {

        LocalDate data =
                LocalDate.now().plusDays(1);

        LocalTime hora =
                LocalTime.of(10, 0);

        when(bloqueioRepository
                .findByDataAndTipo(any(), any()))
                .thenReturn(List.of());

        when(configuracaoAgendaRepository
                .findByTipo("SECRETARIA"))
                .thenReturn(Optional.empty());

        when(marcacaoRepository
                .countByDataAndTipo(any(), any()))
                .thenReturn(1L);

        boolean resultado =
                calendarioService
                        .isSlotBloqueado(
                                data,
                                hora,
                                "SECRETARIA"
                        );

        assertTrue(resultado);
    }

    @Test
    void isSlotBloqueado_DeveRetornarFalse() {

        LocalDate data =
                LocalDate.now().plusDays(1);

        LocalTime hora =
                LocalTime.of(10, 0);

        when(bloqueioRepository
                .findByDataAndTipo(any(), any()))
                .thenReturn(List.of());

        when(configuracaoAgendaRepository
                .findByTipo("SECRETARIA"))
                .thenReturn(Optional.empty());

        when(marcacaoRepository
                .countByDataAndTipo(any(), any()))
                .thenReturn(0L);

        boolean resultado =
                calendarioService
                        .isSlotBloqueado(
                                data,
                                hora,
                                "SECRETARIA"
                        );

        assertFalse(resultado);
    }

    @Test
    void bloquearHorario_DeveCriarBloqueio() {

        LocalDate data =
                LocalDate.now().plusDays(2);

        when(bloqueioRepository
                .countConflictingWithLockByTipo(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(0L);

        when(marcacaoRepository
                .findMarcacoesBetweenDates(
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(List.of());

        when(bloqueioRepository
                .save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Utilizador user =
                new Utilizador();

        BloqueioAgenda resultado =
                calendarioService
                        .bloquearHorario(
                                data,
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0),
                                "Teste",
                                user,
                                "SECRETARIA"
                        );

        assertNotNull(resultado);
        assertEquals("Teste",
                resultado.getMotivo());
    }

    @Test
    void bloquearHorario_DeveLancarErroDataPassada() {

        assertThrows(
                BadRequestException.class,
                () -> calendarioService
                        .bloquearHorario(
                                LocalDate.now().minusDays(1),
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0),
                                "Teste",
                                new Utilizador(),
                                "SECRETARIA"
                        )
        );
    }

    @Test
    void bloquearHorario_DeveLancarErroConflito() {

        when(bloqueioRepository
                .countConflictingWithLockByTipo(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(1L);

        assertThrows(
                BadRequestException.class,
                () -> calendarioService
                        .bloquearHorario(
                                LocalDate.now().plusDays(1),
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0),
                                "Teste",
                                new Utilizador(),
                                "SECRETARIA"
                        )
        );
    }

    @Test
    void bloquearHorario_DeveLancarErroMarcacoesAtivas() {

        Marcacao marcacao =
                new Marcacao();

        marcacao.setEstado(
                EventoEstado.AGENDADO
        );

        marcacao.setData(
                LocalDateTime.now()
                        .plusDays(1)
        );

        when(bloqueioRepository
                .countConflictingWithLockByTipo(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(0L);

        when(marcacaoRepository
                .findMarcacoesBetweenDates(
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(List.of(marcacao));

        assertThrows(
                BadRequestException.class,
                () -> calendarioService
                        .bloquearHorario(
                                LocalDate.now().plusDays(1),
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0),
                                "Teste",
                                new Utilizador(),
                                "SECRETARIA"
                        )
        );
    }

    @Test
    void getTodosBloqueios_DeveRetornarLista() {

        when(bloqueioRepository
                .findAll())
                .thenReturn(List.of(
                        new BloqueioAgenda()
                ));

        List<BloqueioAgenda> resultado =
                calendarioService
                        .getTodosBloqueios(null);

        assertEquals(1, resultado.size());
    }
}