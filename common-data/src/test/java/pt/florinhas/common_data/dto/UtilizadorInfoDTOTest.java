package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilizadorInfoDTOTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        UtilizadorInfoDTO dto =
                new UtilizadorInfoDTO();

        dto.setNome("Teste");
        dto.setEmail("teste@teste.com");
        dto.setTelefone("912345678");
        dto.setDataNasc("2000-01-01");
        dto.setMorada("Rua A");
        dto.setCodigoPostal("1234-123");
        dto.setFreguesia("Aveiro");
        dto.setTelefoneEmprego("911111111");
        dto.setLocalEmprego("Empresa");
        dto.setMoradaEmprego("Rua B");
        dto.setProfissao("Programador");

        assertEquals("Teste", dto.getNome());
        assertEquals("teste@teste.com", dto.getEmail());
        assertEquals("912345678", dto.getTelefone());
        assertEquals("2000-01-01", dto.getDataNasc());
        assertEquals("Rua A", dto.getMorada());
        assertEquals("1234-123", dto.getCodigoPostal());
        assertEquals("Aveiro", dto.getFreguesia());
        assertEquals("911111111", dto.getTelefoneEmprego());
        assertEquals("Empresa", dto.getLocalEmprego());
        assertEquals("Rua B", dto.getMoradaEmprego());
        assertEquals("Programador", dto.getProfissao());
    }

    @Test
    void allArgsConstructor_DeveFuncionar() {

        UtilizadorInfoDTO dto =
                new UtilizadorInfoDTO(
                        "Teste",
                        "teste@teste.com",
                        "912345678",
                        "2000-01-01",
                        "Rua A",
                        "1234-123",
                        "Aveiro",
                        "911111111",
                        "Empresa",
                        "Rua B",
                        "Programador"
                );

        assertEquals("Teste", dto.getNome());
    }
}