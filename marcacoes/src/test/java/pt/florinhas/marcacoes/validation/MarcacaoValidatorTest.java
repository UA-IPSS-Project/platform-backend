package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                org.mockito.Mockito.mock(
                        CalendarioService.class);

        validator =
                new MarcacaoValidator(
                        calendarioService);
    }

    @Test
    void validarCriacao_DeveAceitarRequestValido() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("Teste");

        request.setData(
                LocalDateTime.of(
                        2026,
                        5,
                        18,
                        10,
                        0));

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                any(),
                any(),
                anyString()))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoRequestNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        null));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoAssuntoVazio() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("");

        request.setData(
                LocalDateTime.of(
                        2026,
                        5,
                        18,
                        10,
                        0));

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                any(),
                any(),
                anyString()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoDataPassado() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("Teste");

        request.setData(
                LocalDateTime.now()
                        .minusDays(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoFimSemana() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("Teste");

        request.setData(
                LocalDateTime.of(
                        2026,
                        5,
                        17,
                        10,
                        0));

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoFeriado() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("Teste");

        request.setData(
                LocalDateTime.of(
                        2026,
                        5,
                        18,
                        10,
                        0));

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(
                        List.of(
                                LocalDate.of(
                                        2026,
                                        5,
                                        18)));

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoSlotBloqueado() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("Teste");

        request.setData(
                LocalDateTime.of(
                        2026,
                        5,
                        18,
                        10,
                        0));

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                any(),
                any(),
                anyString()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacao_DeveLancarExcecaoMais365Dias() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest();

        request.setAssunto("Teste");

        request.setData(
                LocalDateTime.now()
                        .plusDays(367));

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(
                        request));
    }

    @Test
    void validarCriacaoBalneario_DeveAceitarRequestValido() {

        CriarMarcacaoBalnearioRequest request =
                new CriarMarcacaoBalnearioRequest();

        request.setData(
                LocalDateTime.of(
                        2026,
                        5,
                        18,
                        10,
                        0));

        request.setRoupas(List.of());

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                any(),
                any(),
                anyString()))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validarCriacaoBalneario(
                        request));
    }

    @Test
    void validarCriacaoBalneario_DeveLancarExcecaoRequestNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacaoBalneario(
                        null));
    }

    @Test
    void validarRoupas_DeveAceitarNull() {

        assertDoesNotThrow(
                () -> validator.validarRoupas(
                        null));
    }

    @Test
    void validarRoupas_DeveAceitarCategoria() {

        RoupaDTO roupa =
                new RoupaDTO();

        roupa.setCategoria("CAMISOLA");

        assertDoesNotThrow(
                () -> validator.validarRoupas(
                        List.of(roupa)));
    }

    @Test
    void validarRoupas_DeveAceitarItemId() {

        RoupaDTO roupa =
                new RoupaDTO();

        roupa.setItemId(1L);

        assertDoesNotThrow(
                () -> validator.validarRoupas(
                        List.of(roupa)));
    }

    @Test
    void validarRoupas_DeveLancarExcecao() {

        RoupaDTO roupa =
                new RoupaDTO();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarRoupas(
                        List.of(roupa)));
    }

    @Test
    void validarReagendamento_DeveAceitarValido() {

        ReagendarMarcacaoRequest request =
                new ReagendarMarcacaoRequest();

        request.setNovaDataHora(
                LocalDateTime.of(
                        2026,
                        5,
                        18,
                        10,
                        0));

        when(calendarioService.getFeriadosDoAno(
                2026))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                any(),
                any(),
                anyString()))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validarReagendamento(
                        request,
                        "SECRETARIA"));
    }

    @Test
    void validarReagendamento_DeveLancarExcecaoRequestNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarReagendamento(
                        null,
                        "SECRETARIA"));
    }

    @Test
    void validarReagendamento_DeveLancarExcecaoNovaDataNull() {

        ReagendarMarcacaoRequest request =
                new ReagendarMarcacaoRequest();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarReagendamento(
                        request,
                        "SECRETARIA"));
    }
}