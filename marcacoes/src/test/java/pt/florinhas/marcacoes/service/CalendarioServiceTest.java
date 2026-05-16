package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {

    @Mock
    private BloqueioRepository bloqueioRepository;
    @Mock
    private MarcacaoRepository marcacaoRepository;
    @Mock
    private ConfiguracaoAgendaRepository configuracaoAgendaRepository;

    private CalendarioService calendarioService;

    @BeforeEach
    void setup() {
        calendarioService = new CalendarioService(bloqueioRepository, marcacaoRepository, configuracaoAgendaRepository);
    }

    private LocalDate nextWeekday() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek().getValue() >= 6)
            d = d.plusDays(1);
        return d;
    }

    @Test
    @DisplayName("Deve lançar erro ao bloquear data no passado")
    void bloquearHorario_DeveLancarExcecaoDataPassada() {
        assertThrows(BadRequestException.class, () -> calendarioService.bloquearHorario(LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "M", new Utilizador(), "SECRETARIA"));
    }

    @Test
    @DisplayName("Deve criar bloqueio com sucesso")
    void bloquearHorario_DeveCriarBloqueioSucesso() {
        LocalDate d = nextWeekday();
        when(bloqueioRepository.countConflictingWithLockByTipo(any(), any(), any(), any())).thenReturn(0L);
        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), any())).thenReturn(List.of());
        when(bloqueioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BloqueioAgenda result = calendarioService.bloquearHorario(d, LocalTime.of(10, 0), LocalTime.of(11, 0), "Motivo",
                new Utilizador(), "SECRETARIA");

        assertNotNull(result);
        assertEquals("Motivo", result.getMotivo());
        verify(bloqueioRepository).save(any());
    }

    @Test
    @DisplayName("Deve indicar slot bloqueado quando atinge capacidade máxima")
    void isSlotBloqueado_DeveRetornarTrueQuandoCapacidadeAtingida() {
        LocalDate d = nextWeekday();
        LocalTime h = LocalTime.of(10, 0);
        when(bloqueioRepository.findByDataAndTipo(d, "SECRETARIA")).thenReturn(List.of());
        when(configuracaoAgendaRepository.findByTipo("SECRETARIA")).thenReturn(Optional.empty());
        when(marcacaoRepository.countByDataAndTipo(d.atTime(h), "SECRETARIA")).thenReturn(1L);

        assertTrue(calendarioService.isSlotBloqueado(d, h, "SECRETARIA"));
    }
}