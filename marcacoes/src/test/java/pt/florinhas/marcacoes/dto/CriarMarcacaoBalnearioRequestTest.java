package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class CriarMarcacaoBalnearioRequestTest {

    @Test
    void deveCriarRequest() {

        RoupaDTO roupa = new RoupaDTO();

        CriarMarcacaoBalnearioRequest request =
                new CriarMarcacaoBalnearioRequest(
                        LocalDateTime.now(),
                        "Utente",
                        true,
                        true,
                        1L,
                        List.of(roupa),
                        "Obs",
                        2L
                );

        assertEquals("Utente", request.getNomeUtente());
        assertTrue(request.getProdutosHigiene());
        assertTrue(request.getLavagemRoupa());
        assertEquals(1L, request.getResponsavelId());
        assertEquals("Obs", request.getObservacoes());
        assertEquals(2L, request.getReservaId());
    }
}