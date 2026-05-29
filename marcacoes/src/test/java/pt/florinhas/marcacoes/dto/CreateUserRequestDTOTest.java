package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CreateUserRequestDTOTest {

    @Test
    void createUserRequestDTO_DeveGuardarValores() {

        CreateUserRequestDTO dto = new CreateUserRequestDTO();

        dto.setName("Nuno");
        dto.setNif("123456789");
        dto.setContact("912345678");
        dto.setEmail("teste@teste.com");
        dto.setBirthDate("2000-01-01");
        dto.setEmployee(true);
        dto.setRole("SECRETARIA");

        assertEquals("Nuno", dto.getName());
        assertEquals("123456789", dto.getNif());
        assertEquals("912345678", dto.getContact());
        assertEquals("teste@teste.com", dto.getEmail());
        assertEquals("2000-01-01", dto.getBirthDate());
        assertEquals(true, dto.isEmployee());
        assertEquals("SECRETARIA", dto.getRole());
    }
}