package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CreateUserRequestDTOTest {

    @Test
    void deveDefinirValores() {

        CreateUserRequestDTO dto =
                new CreateUserRequestDTO();

        dto.setName("Nuno");
        dto.setNif("123456789");
        dto.setContact("912345678");
        dto.setEmail("test@test.com");
        dto.setBirthDate("2000-01-01");
        dto.setEmployee(true);
        dto.setRole("ADMIN");

        assertEquals("Nuno", dto.getName());
        assertEquals("123456789", dto.getNif());
        assertEquals("912345678", dto.getContact());
        assertEquals("test@test.com", dto.getEmail());
        assertEquals("2000-01-01", dto.getBirthDate());
        assertTrue(dto.isEmployee());
        assertEquals("ADMIN", dto.getRole());
    }
}