package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.service.CalendarioService;

class MarcacaoValidatorTest {

    @Mock
    private CalendarioService calendarioService;

    @InjectMocks
    private MarcacaoValidator validator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // HELPER
    // =========================

    private LocalDateTime nextWeekdayDateTime() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date.atTime(10, 0);
    }

    // =========================
    // CRIACAO
    // =========================

    @Test
    void validarCriacao_DeveFalhar_QuandoRequestNull() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(null));
    }

    @Test
    void validarCriacao_DeveFalhar_QuandoAssuntoVazio() {
        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(nextWeekdayDateTime());
        req.setAssunto(" ");

        when(calendarioService.getFeriadosDoAno(anyInt())).thenReturn(List.of());
        when(calendarioService.isSlotBloqueado(any(), any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(req));
    }

    @Test
    void validarCriacao_ComSucesso() {
        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(nextWeekdayDateTime());
        req.setAssunto("Consulta");

        when(calendarioService.getFeriadosDoAno(anyInt())).thenReturn(List.of());
        when(calendarioService.isSlotBloqueado(any(), any(), any())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarCriacao(req));
    }

    // =========================
    // DATA / HORA
    // =========================

    @Test
    void validarDataHora_DeveFalhar_QuandoPassado() {
        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(LocalDateTime.now().minusDays(1));
        req.setAssunto("Consulta");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(req));
    }

    @Test
    void validarDataHora_DeveFalhar_QuandoMuitoFuturo() {
        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(LocalDateTime.now().plusDays(400));
        req.setAssunto("Consulta");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(req));
    }

    @Test
    void validarDataHora_DeveFalhar_QuandoFimDeSemana() {
        LocalDate sabado = LocalDate.now()
                .with(DayOfWeek.SATURDAY);

        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(sabado.atTime(10, 0));
        req.setAssunto("Consulta");

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(req));
    }

    @Test
    void validarDataHora_DeveFalhar_QuandoFeriado() {
        LocalDateTime data = nextWeekdayDateTime();

        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(data);
        req.setAssunto("Consulta");

        when(calendarioService.getFeriadosDoAno(anyInt()))
                .thenReturn(List.of(data.toLocalDate()));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(req));
    }

    @Test
    void validarDataHora_DeveFalhar_QuandoSlotBloqueado() {
        LocalDateTime data = nextWeekdayDateTime();

        CriarMarcacaoRequest req = new CriarMarcacaoRequest();
        req.setData(data);
        req.setAssunto("Consulta");

        when(calendarioService.getFeriadosDoAno(anyInt())).thenReturn(List.of());
        when(calendarioService.isSlotBloqueado(any(), any(), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarCriacao(req));
    }

    // =========================
    // ROUPAS
    // =========================

    @Test
    void validarRoupas_DeveFalhar_QuandoSemCategoriaNemItem() {
        RoupaDTO r = new RoupaDTO();

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarRoupas(List.of(r)));
    }

    @Test
    void validarRoupas_DeveAceitarCategoria() {
        RoupaDTO r = new RoupaDTO();
        r.setCategoria("Casaco");

        assertDoesNotThrow(() -> validator.validarRoupas(List.of(r)));
    }

    @Test
    void validarRoupas_DeveAceitarItem() {
        RoupaDTO r = new RoupaDTO();
        r.setItemId(1L);

        assertDoesNotThrow(() -> validator.validarRoupas(List.of(r)));
    }

    // =========================
    // REAGENDAMENTO
    // =========================

    @Test
    void validarReagendamento_DeveFalhar_QuandoNull() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validarReagendamento(null, "SECRETARIA"));
    }

    @Test
    void validarReagendamento_DeveFalhar_QuandoDataNull() {
        ReagendarMarcacaoRequest req = new ReagendarMarcacaoRequest();

        assertThrows(IllegalArgumentException.class,
                () -> validator.validarReagendamento(req, "SECRETARIA"));
    }

    @Test
    void validarReagendamento_ComSucesso() {
        ReagendarMarcacaoRequest req = new ReagendarMarcacaoRequest();
        req.setNovaDataHora(nextWeekdayDateTime());

        when(calendarioService.getFeriadosDoAno(anyInt())).thenReturn(List.of());
        when(calendarioService.isSlotBloqueado(any(), any(), any())).thenReturn(false);

        assertDoesNotThrow(() ->
                validator.validarReagendamento(req, "SECRETARIA"));
    }
}