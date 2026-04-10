package pt.florinhas.marcacoes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.exception.BadRequestException;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {

    @Mock private BloqueioRepository bloqueioRepository;
    @Mock private MarcacaoRepository marcacaoRepository;
    @Mock private ConfiguracaoAgendaRepository configuracaoAgendaRepository;

    @InjectMocks
    private CalendarioService calendarioService;

    private LocalDate nextWeekday() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    // ======================
    // CAPACIDADE
    // ======================

    @Test
    void shouldReturnDefaultSecretariaCapacity() {
        assertEquals(1, calendarioService.getCapacidadePorSlot(null));
    }

    @Test
    void shouldReturnDefaultBalnearioCapacity() {
        when(configuracaoAgendaRepository.findByTipo("BALNEARIO")).thenReturn(Optional.empty());

        assertEquals(2, calendarioService.getCapacidadePorSlot("BALNEARIO"));
    }

    @Test
    void shouldReturnConfiguredCapacity() {
        ConfiguracaoAgenda cfg = new ConfiguracaoAgenda();
        cfg.setTipo("SECRETARIA");
        cfg.setCapacidadePorSlot(5);

        when(configuracaoAgendaRepository.findByTipo("SECRETARIA"))
                .thenReturn(Optional.of(cfg));

        assertEquals(5, calendarioService.getCapacidadePorSlot("SECRETARIA"));
    }

    @Test
    void shouldCreateConfigWhenMissing() {
        when(configuracaoAgendaRepository.findByTipo("SECRETARIA"))
                .thenReturn(Optional.empty());

        when(configuracaoAgendaRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        ConfiguracaoSlotDTO dto =
                calendarioService.atualizarCapacidadePorSlot("SECRETARIA", 3);

        assertEquals("SECRETARIA", dto.getTipo());
        assertEquals(3, dto.getCapacidadePorSlot());
    }

    @Test
    void shouldThrowInvalidType() {
        assertThrows(BadRequestException.class,
                () -> calendarioService.atualizarCapacidadePorSlot("X", 2));
    }

    @Test
    void shouldThrowInvalidCapacity() {
        assertThrows(BadRequestException.class,
                () -> calendarioService.atualizarCapacidadePorSlot("SECRETARIA", 0));
    }

    // ======================
    // BLOQUEIO HORÁRIO
    // ======================

    @Test
    void shouldThrowPastDate() {
        assertThrows(BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        LocalDate.now().minusDays(1),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Motivo",
                        new Utilizador(),
                        "SECRETARIA"
                ));
    }

    @Test
    void shouldThrowOutsideHours() {
        LocalDate d = nextWeekday();

        assertThrows(BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        d,
                        LocalTime.of(8, 0),
                        LocalTime.of(10, 0),
                        "Motivo",
                        new Utilizador(),
                        "SECRETARIA"
                ));
    }

    @Test
    void shouldThrowInvalidInterval() {
        LocalDate d = nextWeekday();

        assertThrows(BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        d,
                        LocalTime.of(11, 0),
                        LocalTime.of(10, 0),
                        "Motivo",
                        new Utilizador(),
                        "SECRETARIA"
                ));
    }

    @Test
void shouldThrowWeekend() {
    LocalDate temp = LocalDate.now();
    while (temp.getDayOfWeek() != DayOfWeek.SATURDAY) {
        temp = temp.plusDays(1);
    }
    final LocalDate d = temp;

    assertThrows(BadRequestException.class,
            () -> calendarioService.bloquearHorario(
                    d,
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    "Motivo",
                    new Utilizador(),
                    "SECRETARIA"
            ));
}

    @Test
    void shouldThrowConflict() {
        LocalDate d = nextWeekday();

        when(bloqueioRepository.countConflictingWithLockByTipo(
                d, LocalTime.of(10, 0), LocalTime.of(11, 0), "SECRETARIA"
        )).thenReturn(1L);

        assertThrows(BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        d,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Motivo",
                        new Utilizador(),
                        "SECRETARIA"
                ));
    }

    @Test
    void shouldThrowWhenAppointmentsExist() {
        LocalDate d = nextWeekday();

        Marcacao m = new Marcacao();
        m.setEstado(EventoEstado.AGENDADO);
        m.setData(d.atTime(10, 30));

        when(bloqueioRepository.countConflictingWithLockByTipo(any(), any(), any(), any()))
                .thenReturn(0L);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(), any(), any()
        )).thenReturn(List.of(m));

        assertThrows(BadRequestException.class,
                () -> calendarioService.bloquearHorario(
                        d,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Motivo",
                        new Utilizador(),
                        "SECRETARIA"
                ));
    }

    @Test
    void shouldCreateBlockSuccessfully() {
        LocalDate d = nextWeekday();

        when(bloqueioRepository.countConflictingWithLockByTipo(any(), any(), any(), any()))
                .thenReturn(0L);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), any()))
                .thenReturn(List.of());

        when(bloqueioRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        BloqueioAgenda result =
                calendarioService.bloquearHorario(
                        d,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        "Motivo",
                        new Utilizador(),
                        "SECRETARIA"
                );

        assertEquals(d, result.getData());
        assertEquals("Motivo", result.getMotivo());
    }

    // ======================
    // SLOT BLOQUEADO
    // ======================

    @Test
    void shouldReturnBlockedWhenFullCapacity() {
        LocalDate d = nextWeekday();
        LocalTime h = LocalTime.of(10, 0);

        when(bloqueioRepository.findByDataAndTipo(d, "SECRETARIA"))
                .thenReturn(List.of());

        when(configuracaoAgendaRepository.findByTipo("SECRETARIA"))
                .thenReturn(Optional.empty());

        when(marcacaoRepository.countByDataAndTipo(d.atTime(h), "SECRETARIA"))
                .thenReturn(1L);

        assertTrue(calendarioService.isSlotBloqueado(d, h, "SECRETARIA"));
    }

    @Test
    void shouldReturnBlockedWhenPartialBlockExists() {
        LocalDate d = nextWeekday();
        LocalTime h = LocalTime.of(10, 30);

        BloqueioAgenda b = new BloqueioAgenda();
        b.setHoraInicio(LocalTime.of(10, 0));
        b.setHoraFim(LocalTime.of(11, 0));

        when(bloqueioRepository.findByDataAndTipo(d, "SECRETARIA"))
                .thenReturn(List.of(b));

        assertTrue(calendarioService.isSlotBloqueado(d, h, "SECRETARIA"));
    }

    // ======================
    // CONSULTAS
    // ======================

    @Test
    void shouldRemoveBlock() {
        calendarioService.removerBloqueio(1L);
        verify(bloqueioRepository).deleteById(1L);
    }

    @Test
    void shouldGetBlocksOfMonthByType() {
        when(bloqueioRepository.findByDataBetweenAndTipo(any(), any(), eq("SECRETARIA")))
                .thenReturn(List.of(new BloqueioAgenda()));

        assertEquals(1,
                calendarioService.getBloqueiosDoMes(2026, 4, "SECRETARIA").size());
    }

    @Test
    void shouldGetAllBlocks() {
        when(bloqueioRepository.findAll())
                .thenReturn(List.of(new BloqueioAgenda()));

        assertEquals(1,
                calendarioService.getTodosBloqueios(null).size());
    }
}