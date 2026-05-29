package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DocumentoTest {

    @Test
    void onCreate_DeveDefinirUploadedEm() {

        Documento documento = new Documento();

        documento.onCreate();

        assertNotNull(documento.getUploadedEm());
    }
}