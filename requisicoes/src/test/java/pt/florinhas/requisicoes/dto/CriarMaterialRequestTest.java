package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;

class CriarMaterialRequestTest
        extends BaseValidatorTest {

    @Test
    void record_DeveGuardarValores() {

        CriarMaterialRequest request =
                new CriarMaterialRequest(
                        "Caneta",
                        "ESCRITA",
                        "Cor",
                        "Azul");

        assertEquals("Caneta", request.nome());
        assertEquals("ESCRITA", request.categoria());
        assertEquals("Cor", request.atributo());
        assertEquals("Azul", request.valorAtributo());
    }
    @Test
    void deveFalharQuandoNomeVazio() {

        CriarMaterialRequest request =
                new CriarMaterialRequest(
                        "",
                        "ESCRITA",
                        null,
                        null);

        Set<ConstraintViolation<CriarMaterialRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("nome")));
    }

    @Test
    void deveFalharQuandoNomeNull() {

        CriarMaterialRequest request =
                new CriarMaterialRequest(
                        null,
                        "ESCRITA",
                        null,
                        null);

        Set<ConstraintViolation<CriarMaterialRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoCategoriaNull() {

        CriarMaterialRequest request =
                new CriarMaterialRequest(
                        "Caneta",
                        null,
                        null,
                        null);

        Set<ConstraintViolation<CriarMaterialRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("categoria")));
    }
    @Test
    void devePassarQuandoDadosValidos() {

        CriarMaterialRequest request =
                new CriarMaterialRequest(
                        "Caneta",
                        "ESCRITA",
                        "Cor",
                        "Azul");

        Set<ConstraintViolation<CriarMaterialRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}