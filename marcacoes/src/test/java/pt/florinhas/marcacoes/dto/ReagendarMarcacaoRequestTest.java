package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ReagendarMarcacaoRequestTest {

    @Test
    void deveDefinirNovaDataHora() {

        ReagendarMarcacaoRequest request =
                new ReagendarMarcacaoRequest();

        LocalDateTime novaData =
                LocalDateTime.now().plusDays(1);

        request.setNovaDataHora(novaData);

        assertEquals(
                novaData,
                request.getNovaDataHora()
        );
    }
}