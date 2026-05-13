package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
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
                new MarcacaoValidator(calendarioService);
    }

    @Test
    @DisplayName("Deve validar criação corretamente")
    void validarCriacao_DeveValidar() {

        CriarMarcacaoRequest request =
                mock(CriarMarcacaoRequest.class);

        LocalDateTime data =
                LocalDateTime.now().plusDays(1);

        when(request.getData())
                .thenReturn(data);

        when(request.getAssunto())
                .thenReturn("Consulta");

        when(calendarioService.getFeriadosDoAno(anyInt()))
                .thenReturn(List.of());

        when(calendarioService.isSlotBloqueado(
                any(),
                any(),
                any()
        )).thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validarCriacao(request)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para request null")
    void validarCriacao_DeveLancarException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarCriacao(null)
        );
    }

    @Test
    @DisplayName("Deve validar roupas")
    void validarRoupas_DeveValidar() {

        RoupaDTO roupa =
                new RoupaDTO();

        roupa.setCategoria("Casaco");

        assertDoesNotThrow(
                () -> validator.validarRoupas(
                        List.of(roupa)
                )
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para roupa inválida")
    void validarRoupas_DeveLancarException() {

        RoupaDTO roupa =
                new RoupaDTO();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarRoupas(
                        List.of(roupa)
                )
        );
    }
}