package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ValenciaTest {

    @Test
    void valencia_DeveGuardarValores() {

        Valencia valencia =
                new Valencia();

        valencia.setId(1L);
        valencia.setNome("Balneário");
        valencia.setDescricao("Descrição");

        assertEquals(1L, valencia.getId());
        assertEquals("Balneário", valencia.getNome());
        assertEquals("Descrição", valencia.getDescricao());
    }
}