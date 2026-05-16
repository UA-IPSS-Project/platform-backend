package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class NotificacaoResponseDTOTest {

    @Test
    void gettersSetters_DeveFuncionar() {

        NotificacaoResponseDTO dto =
                new NotificacaoResponseDTO();

        LocalDateTime data =
                LocalDateTime.now();

        Map<String, Object> metadata =
                Map.of("id", 1);

        dto.setId(1L);
        dto.setTitulo("Titulo");
        dto.setMensagem("Mensagem");
        dto.setTipo("INFO");
        dto.setLida(true);
        dto.setDataCriacao(data);
        dto.setUtilizadorId(5L);
        dto.setMetadata(metadata);

        assertEquals(1L, dto.getId());
        assertEquals("Titulo", dto.getTitulo());
        assertEquals("Mensagem", dto.getMensagem());
        assertEquals("INFO", dto.getTipo());
        assertEquals(true, dto.isLida());
        assertEquals(data, dto.getDataCriacao());
        assertEquals(5L, dto.getUtilizadorId());
        assertEquals(metadata, dto.getMetadata());
    }

    @Test
    void lida_DeveSerFalsePorDefeito() {

        NotificacaoResponseDTO dto =
                new NotificacaoResponseDTO();

        assertFalse(dto.isLida());
    }
}