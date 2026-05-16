package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class UtenteRegisterRequestTest {

    @Test
    void constructor_DeveCriarRequest() {

        LocalDate data =
                LocalDate.of(2000, 5, 10);

        UtenteRegisterRequest request =
                new UtenteRegisterRequest(
                        "Utente",
                        "utente@teste.com",
                        "password",
                        "123456789",
                        "912345678",
                        data,
                        true
                );

        assertEquals(
                "Utente",
                request.nome());

        assertEquals(
                "utente@teste.com",
                request.email());

        assertEquals(
                "password",
                request.password());

        assertEquals(
                "123456789",
                request.nif());

        assertEquals(
                "912345678",
                request.telefone());

        assertEquals(
                data,
                request.dataNasc());

        assertTrue(
                request.termsAccepted());
    }
}