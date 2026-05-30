package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.service.CalendarioService;

class MarcacaoValidatorTest {

    private CalendarioService calendarioService;
    private MarcacaoValidator validator;

    @BeforeEach
    void setUp() {

        calendarioService =
                mock(CalendarioService.class);

        validator =
                new MarcacaoValidator(
                        calendarioService);
    }

    @Test
    void validarCriacao_DeveAceitarPedidoValido() {

        LocalDateTime data =
                LocalDate.now()
                        .with(TemporalAdjusters.next(
                                java.time.DayOfWeek.MONDAY))
                        .atTime(10, 0);

        when(calendarioService.getFeriadosDoAno(data.getYear()))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                data.toLocalDate(),
                data.toLocalTime(),
                "SECRETARIA"))
                .thenReturn(false);

        CriarMarcacaoRequest request =
                mock(CriarMarcacaoRequest.class);

        when(request.getData())
                .thenReturn(data);

        when(request.getAssunto())
                .thenReturn("Teste");

        assertDoesNotThrow(
                () -> validator.validarCriacao(request));
    }

    @Test
    void validarCriacao_DeveFalharQuandoNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(null));
    }

    @Test
    void validarCriacaoBalneario_DeveAceitarPedidoValido() {

        LocalDateTime data =
                LocalDate.now()
                        .with(TemporalAdjusters.next(
                                java.time.DayOfWeek.MONDAY))
                        .atTime(10, 0);

        when(calendarioService.getFeriadosDoAno(data.getYear()))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                data.toLocalDate(),
                data.toLocalTime(),
                "BALNEARIO"))
                .thenReturn(false);

        CriarMarcacaoBalnearioRequest request =
                mock(CriarMarcacaoBalnearioRequest.class);

        when(request.getData())
                .thenReturn(data);

        when(request.getRoupas())
                .thenReturn(List.of());

        assertDoesNotThrow(
                () -> validator.validarCriacaoBalneario(request));
    }

    @Test
    void validarRoupas_DeveFalharSemCategoriaNemItem() {

        RoupaDTO roupa =
                mock(RoupaDTO.class);

        when(roupa.getCategoria())
                .thenReturn(null);

        when(roupa.getItemId())
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarRoupas(
                        List.of(roupa)));
    }

    @Test
    void validarReagendamento_DeveFalharQuandoNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarReagendamento(
                        null,
                        "SECRETARIA"));
    }

    @Test
    void validarReagendamento_DeveFalharSemData() {

        ReagendarMarcacaoRequest request =
                mock(ReagendarMarcacaoRequest.class);

        when(request.getNovaDataHora())
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarReagendamento(
                        request,
                        "SECRETARIA"));
    }

    @Test
    void validarCriacao_DeveFalharFimSemana() {

        LocalDateTime sabado =
                LocalDate.now()
                        .with(java.time.DayOfWeek.SATURDAY)
                        .atTime(LocalTime.of(10, 0));

        if (sabado.isBefore(LocalDateTime.now())) {
            sabado = sabado.plusWeeks(1);
        }

        CriarMarcacaoRequest request =
                mock(CriarMarcacaoRequest.class);

        when(request.getData())
                .thenReturn(sabado);

        when(request.getAssunto())
                .thenReturn("Teste");

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(request));
    }
}