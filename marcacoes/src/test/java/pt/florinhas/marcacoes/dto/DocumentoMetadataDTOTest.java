package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DocumentoMetadataDTOTest {

    @Test
    void deveCriarMetadataDTO() {

        DocumentoMetadataDTO dto =
                new DocumentoMetadataDTO(
                        1L,
                        "teste.pdf",
                        "abc.pdf",
                        "/tmp",
                        "application/pdf",
                        100L,
                        LocalDateTime.now(),
                        2L,
                        "etag",
                        "2025-01-01",
                        Map.of("k", "v"),
                        1
                );

        assertEquals(1L, dto.id());
        assertEquals("teste.pdf", dto.nomeOriginal());
        assertEquals("abc.pdf", dto.nomeArmazenado());
        assertEquals("/tmp", dto.caminho());
    }
}