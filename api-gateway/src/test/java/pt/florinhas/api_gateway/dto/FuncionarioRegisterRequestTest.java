package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class FuncionarioRegisterRequestTest {

    @Test
    void constructor_DeveCriarRequest() {

        LocalDate data =
                LocalDate.of(1990, 1, 1);

        FuncionarioRegisterRequest request =
                new FuncionarioRegisterRequest(
                        "Teste",
                        "teste@teste.com",
                        "password",
                        "123456789",
                        "912345678",
                        "SECRETARIA",
                        data,
                        true
                );

        assertEquals("Teste", request.nome());
        assertEquals("teste@teste.com", request.email());
        assertEquals("password", request.password());
        assertEquals("123456789", request.nif());
        assertEquals("912345678", request.contacto());
        assertEquals("SECRETARIA", request.funcao());
        assertEquals(data, request.dataNasc());
        assertTrue(request.termsAccepted());
    }
}