package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DocumentoTest {

    @Test
    void onCreateDeveDefinirDataUpload() {

        Documento documento = new Documento();

        documento.onCreate();

        assertNotNull(documento.getUploadedEm());
    }

    @Test
    void deveCriarDocumento() {

        Documento documento = new Documento();

        documento.setId(1L);
        documento.setNomeOriginal("teste.pdf");
        documento.setNomeArmazenado("abc.pdf");
        documento.setCaminho("/uploads/teste.pdf");
        documento.setTipo("application/pdf");
        documento.setTamanho(100L);

        assertNotNull(documento);
        assertNotNull(documento.getNomeOriginal());
    }
}