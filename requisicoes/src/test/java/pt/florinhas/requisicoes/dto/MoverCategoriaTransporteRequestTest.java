package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

class MoverCategoriaTransporteRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoDadosValidos() {

        MoverCategoriaTransporteRequest request =
                new MoverCategoriaTransporteRequest(
                        TransporteCategoria.ESCOLAR,
                        TransporteCategoria
                                .ABATIDO_VENDIDO_DESCONTINUADO);

        Set<ConstraintViolation<MoverCategoriaTransporteRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoOrigemNull() {

        MoverCategoriaTransporteRequest request =
                new MoverCategoriaTransporteRequest(
                        null,
                        TransporteCategoria.OUTRO);

        Set<ConstraintViolation<MoverCategoriaTransporteRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("origem")));
    }

    @Test
    void deveFalharQuandoDestinoNull() {

        MoverCategoriaTransporteRequest request =
                new MoverCategoriaTransporteRequest(
                        TransporteCategoria.OUTRO,
                        null);

        Set<ConstraintViolation<MoverCategoriaTransporteRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("destino")));
    }

    @Test
    void deveFalharQuandoAmbosNull() {

        MoverCategoriaTransporteRequest request =
                new MoverCategoriaTransporteRequest(
                        null,
                        null);

        Set<ConstraintViolation<MoverCategoriaTransporteRequest>> violations =
                validator.validate(request);

        assertEquals(2, violations.size());
    }
}