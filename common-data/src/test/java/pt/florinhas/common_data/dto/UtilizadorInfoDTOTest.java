package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilizadorInfoDTOTest {

    @Test
    void utilizadorInfoDTO_DeveGuardarValores() {

        UtilizadorInfoDTO dto =
                new UtilizadorInfoDTO(
                        "Teste",
                        "teste@teste.com",
                        "912345678",
                        "2000-01-01",
                        "Rua",
                        "1234-123",
                        "Aveiro",
                        "912345678",
                        "Empresa",
                        "Rua Empresa",
                        "Professor");

        assertEquals("Teste", dto.getNome());
        assertEquals("teste@teste.com", dto.getEmail());
        assertEquals("912345678", dto.getTelefone());
        assertEquals("2000-01-01", dto.getDataNasc());
    }
}