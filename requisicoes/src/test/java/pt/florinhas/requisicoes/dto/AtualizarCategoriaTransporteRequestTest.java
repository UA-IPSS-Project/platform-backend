package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

class AtualizarCategoriaTransporteRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoCategoriaValida() {

        AtualizarCategoriaTransporteRequest request =
                new AtualizarCategoriaTransporteRequest(
                        TransporteCategoria.ESCOLAR);

        Set<ConstraintViolation<AtualizarCategoriaTransporteRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoCategoriaNull() {

        AtualizarCategoriaTransporteRequest request =
                new AtualizarCategoriaTransporteRequest(
                        null);

        Set<ConstraintViolation<AtualizarCategoriaTransporteRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertEquals(1, violations.size());
    }
}