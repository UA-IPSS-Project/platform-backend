package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class ValenciaTest {

    @Test
    void gettersSetters_DeveFuncionar() {

        Valencia valencia =
                new Valencia();

        valencia.setId(1L);

        valencia.setNome(
                "Balneário");

        valencia.setDescricao(
                "Descrição");

        valencia.setFuncionarios(
                new HashSet<>());

        assertEquals(
                1L,
                valencia.getId());

        assertEquals(
                "Balneário",
                valencia.getNome());

        assertEquals(
                "Descrição",
                valencia.getDescricao());

        assertNotNull(
                valencia.getFuncionarios());
    }
}