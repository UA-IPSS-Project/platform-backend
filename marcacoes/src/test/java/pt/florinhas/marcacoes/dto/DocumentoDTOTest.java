package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DocumentoDTOTest {

    @Test
    void deveCriarDTO() {

        DocumentoDTO dto =
                new DocumentoDTO(
                        1L,
                        "teste.pdf",
                        "application/pdf",
                        100L,
                        LocalDateTime.now(),
                        2L,
                        "Nuno",
                        "123456789",
                        1,
                        "RELATORIO"
                );

        assertEquals(1L, dto.id());
        assertEquals("teste.pdf", dto.nomeOriginal());
        assertEquals("application/pdf", dto.tipo());
        assertEquals(100L, dto.tamanho());
        assertEquals(2L, dto.marcacaoId());
    }
}