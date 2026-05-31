package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CriarMarcacaoRequestTest {

    @Test
    void criarMarcacaoRequest_DeveGuardarValores() {

        CriarMarcacaoRequest request = new CriarMarcacaoRequest();

        request.setData(LocalDateTime.now());
        request.setAssunto("Teste");
        request.setUtenteId(1L);
        request.setFuncionarioId(2L);
        request.setCriadoPorId(3L);
        request.setUtenteNif("123456789");
        request.setUtenteNome("Nuno");
        request.setUtenteEmail("teste@teste.com");
        request.setUtenteTelefone("912345678");
        request.setUtenteDataNasc(LocalDate.now());
        request.setDescricao("Descrição");
        request.setTipoAgenda("SECRETARIA");

        assertEquals("Teste", request.getAssunto());
        assertEquals(1L, request.getUtenteId());
        assertEquals(2L, request.getFuncionarioId());
        assertEquals(3L, request.getCriadoPorId());
        assertEquals("123456789", request.getUtenteNif());
        assertEquals("Nuno", request.getUtenteNome());
        assertEquals("teste@teste.com", request.getUtenteEmail());
        assertEquals("912345678", request.getUtenteTelefone());
        assertEquals("Descrição", request.getDescricao());
        assertEquals("SECRETARIA", request.getTipoAgenda());
    }
}