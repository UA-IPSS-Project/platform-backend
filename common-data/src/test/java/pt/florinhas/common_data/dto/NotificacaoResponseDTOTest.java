package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class NotificacaoResponseDTOTest {

    @Test
    void notificacaoResponseDTO_DeveGuardarValores() {

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
        dto.setUtilizadorId(2L);
        dto.setMetadata(Map.of("a", "b"));

        assertEquals(1L, dto.getId());
        assertEquals("Titulo", dto.getTitulo());
        assertEquals("Mensagem", dto.getMensagem());
        assertEquals("INFO", dto.getTipo());
        assertEquals(true, dto.isLida());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(2L, dto.getUtilizadorId());
    }
}