package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CriarMarcacaoRequestTest {

    @Test
    void deveCriarRequest() {

        CriarMarcacaoRequest request =
                new CriarMarcacaoRequest(
                        LocalDateTime.now(),
                        "Consulta",
                        1L,
                        2L,
                        3L,
                        "123456789",
                        "Nuno",
                        "test@test.com",
                        "912345678",
                        LocalDate.now(),
                        "Descricao",
                        "SECRETARIA"
                );

        assertEquals("Consulta", request.getAssunto());
        assertEquals(1L, request.getUtenteId());
        assertEquals(2L, request.getFuncionarioId());
        assertEquals(3L, request.getCriadoPorId());
        assertEquals("123456789", request.getUtenteNif());
        assertEquals("Nuno", request.getUtenteNome());
        assertEquals("test@test.com", request.getUtenteEmail());
        assertEquals("912345678", request.getUtenteTelefone());
        assertEquals("Descricao", request.getDescricao());
        assertEquals("SECRETARIA", request.getTipoAgenda());
    }
}