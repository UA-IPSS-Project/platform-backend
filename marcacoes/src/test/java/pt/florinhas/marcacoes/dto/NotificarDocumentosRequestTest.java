package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NotificarDocumentosRequestTest {

    @Test
    void deveCriarRequest() {

        NotificarDocumentosRequest request =
                new NotificarDocumentosRequest(
                        "Documentos inválidos",
                        1L
                );

        assertEquals(
                "Documentos inválidos",
                request.getObservacoes()
        );

        assertEquals(
                1L,
                request.getFuncionarioId()
        );
    }

    @Test
    void deveUsarSetters() {

        NotificarDocumentosRequest request =
                new NotificarDocumentosRequest();

        request.setObservacoes("Teste");
        request.setFuncionarioId(2L);

        assertEquals(
                "Teste",
                request.getObservacoes()
        );

        assertEquals(
                2L,
                request.getFuncionarioId()
        );
    }
}