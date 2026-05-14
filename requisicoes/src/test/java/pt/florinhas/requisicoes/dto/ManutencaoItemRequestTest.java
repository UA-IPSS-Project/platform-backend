package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;

class ManutencaoItemRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoDadosValidos() {

        ManutencaoItemRequest request =
                new ManutencaoItemRequest(
                        1L,
                        2L,
                        "Observações");

        Set<ConstraintViolation<ManutencaoItemRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoItemIdNull() {

        ManutencaoItemRequest request =
                new ManutencaoItemRequest(
                        null,
                        2L,
                        "Observações");

        Set<ConstraintViolation<ManutencaoItemRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertEquals(1, violations.size());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("itemId")));
    }

    @Test
    void devePermitirCamposOpcionaisNull() {

        ManutencaoItemRequest request =
                new ManutencaoItemRequest(
                        1L,
                        null,
                        null);

        Set<ConstraintViolation<ManutencaoItemRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}