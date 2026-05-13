package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValenciaTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        Valencia valencia =
                new Valencia();

        valencia.setId(1L);
        valencia.setNome("Valencia");
        valencia.setDescricao("Descricao");

        assertEquals(
                1L,
                valencia.getId()
        );

        assertEquals(
                "Valencia",
                valencia.getNome()
        );

        assertEquals(
                "Descricao",
                valencia.getDescricao()
        );
    }
}