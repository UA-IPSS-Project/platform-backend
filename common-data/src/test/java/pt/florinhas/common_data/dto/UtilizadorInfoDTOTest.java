package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilizadorInfoDTOTest {

    @Test
    void construtorVazio_DeveFuncionar() {

        UtilizadorInfoDTO dto =
                new UtilizadorInfoDTO();

        dto.setNome("Teste");
        dto.setEmail("teste@email.com");

        assertEquals("Teste", dto.getNome());
        assertEquals("teste@email.com", dto.getEmail());
    }

    @Test
    void construtorCompleto_DeveFuncionar() {

        UtilizadorInfoDTO dto =
                new UtilizadorInfoDTO(
                        "Nome",
                        "email@email.com",
                        "912345678",
                        "2000-01-01",
                        "Morada",
                        "1234-123",
                        "Freguesia",
                        "999999999",
                        "Empresa",
                        "Morada Empresa",
                        "Programador");

        assertEquals("Nome", dto.getNome());
        assertEquals("email@email.com", dto.getEmail());
        assertEquals("912345678", dto.getTelefone());
        assertEquals("2000-01-01", dto.getDataNasc());
        assertEquals("Morada", dto.getMorada());
        assertEquals("1234-123", dto.getCodigoPostal());
        assertEquals("Freguesia", dto.getFreguesia());
        assertEquals("999999999", dto.getTelefoneEmprego());
        assertEquals("Empresa", dto.getLocalEmprego());
        assertEquals("Morada Empresa", dto.getMoradaEmprego());
        assertEquals("Programador", dto.getProfissao());
    }
}