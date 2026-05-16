package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class CriarBalnearioRequestTest {

    @Test
    void deveDefinirValores() {

        CriarBalnearioRequest request =
                new CriarBalnearioRequest();

        request.setData(LocalDate.now());
        request.setHora(LocalTime.of(10, 0));
        request.setUtenteId(1L);
        request.setFuncionarioId(2L);
        request.setTipoCriador("TECNICO");
        request.setProdutosHigiene(true);
        request.setLavagemRoupa(true);
        request.setRoupaDescricao("Teste");

        assertEquals(1L, request.getUtenteId());
        assertEquals(2L, request.getFuncionarioId());
        assertEquals("TECNICO", request.getTipoCriador());
        assertTrue(request.getProdutosHigiene());
        assertTrue(request.getLavagemRoupa());
        assertEquals("Teste", request.getRoupaDescricao());
    }
}