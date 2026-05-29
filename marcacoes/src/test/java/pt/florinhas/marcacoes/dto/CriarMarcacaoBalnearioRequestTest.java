package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class CriarMarcacaoBalnearioRequestTest {

    @Test
    void criarMarcacaoBalnearioRequest_DeveGuardarValores() {

        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();

        request.setData(LocalDateTime.now());
        request.setNomeUtente("Nuno");
        request.setProdutosHigiene(true);
        request.setLavagemRoupa(false);
        request.setResponsavelId(1L);
        request.setRoupas(List.of());
        request.setObservacoes("Teste");
        request.setReservaId(2L);

        assertEquals("Nuno", request.getNomeUtente());
        assertEquals(true, request.getProdutosHigiene());
        assertEquals(false, request.getLavagemRoupa());
        assertEquals(1L, request.getResponsavelId());
        assertEquals("Teste", request.getObservacoes());
        assertEquals(2L, request.getReservaId());
    }
}