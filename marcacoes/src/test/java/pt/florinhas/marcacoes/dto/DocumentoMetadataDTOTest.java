package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DocumentoMetadataDTOTest {

    @Test
    void documentoMetadataDTO_DeveGuardarValores() {

        DocumentoMetadataDTO dto = new DocumentoMetadataDTO(
                        1L,
                        "teste.pdf",
                        "abc.pdf",
                        "/tmp/teste.pdf",
                        "application/pdf",
                        100L,
                        LocalDateTime.now(),
                        2L,
                        "etag",
                        "2026-01-01",
                        java.util.Map.of("a", "b"),
                        1);

        assertEquals(1L, dto.id());
        assertEquals("teste.pdf", dto.nomeOriginal());
        assertEquals("abc.pdf", dto.nomeArmazenado());
        assertEquals("/tmp/teste.pdf", dto.caminho());
        assertEquals("application/pdf", dto.tipo());
        assertEquals(100L, dto.tamanho());
        assertEquals(2L, dto.marcacaoId());
        assertEquals("etag", dto.etag());
    }
}