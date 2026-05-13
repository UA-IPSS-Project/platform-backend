package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class NotificacaoResponseDTOTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        NotificacaoResponseDTO dto =
                new NotificacaoResponseDTO();

        LocalDateTime now =
                LocalDateTime.now();

        dto.setId(1L);
        dto.setTitulo("Titulo");
        dto.setMensagem("Mensagem");
        dto.setTipo("INFO");
        dto.setLida(true);
        dto.setDataCriacao(now);
        dto.setUtilizadorId(10L);
        dto.setMetadata(Map.of("key", "value"));

        assertEquals(1L, dto.getId());
        assertEquals("Titulo", dto.getTitulo());
        assertEquals("Mensagem", dto.getMensagem());
        assertEquals("INFO", dto.getTipo());
        assertTrue(dto.isLida());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(10L, dto.getUtilizadorId());
        assertEquals("value", dto.getMetadata().get("key"));
    }
}