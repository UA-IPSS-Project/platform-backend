package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecoverAccountDTOTest {

    @Test
    void recoverAccountDTO_DeveGuardarValores() {

        RecoverAccountDTO dto = new RecoverAccountDTO();

        dto.setNif("123456789");
        dto.setUpdatedEmail("teste@teste.com");
        dto.setUpdatedContact("912345678");

        assertEquals("123456789", dto.getNif());
        assertEquals("teste@teste.com", dto.getUpdatedEmail());
        assertEquals("912345678", dto.getUpdatedContact());
    }
}