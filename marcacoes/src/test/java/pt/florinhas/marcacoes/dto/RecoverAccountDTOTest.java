package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RecoverAccountDTOTest {

    @Test
    void deveDefinirValores() {

        RecoverAccountDTO dto =
                new RecoverAccountDTO();

        dto.setNif("123456789");
        dto.setUpdatedEmail("novo@test.com");
        dto.setUpdatedContact("912345678");

        assertEquals(
                "123456789",
                dto.getNif()
        );

        assertEquals(
                "novo@test.com",
                dto.getUpdatedEmail()
        );

        assertEquals(
                "912345678",
                dto.getUpdatedContact()
        );
    }
}