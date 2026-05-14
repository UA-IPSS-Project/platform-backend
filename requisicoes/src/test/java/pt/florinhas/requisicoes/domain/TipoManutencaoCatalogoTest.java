package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TipoManutencaoCatalogoTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        TipoManutencaoCatalogo tipo =
                new TipoManutencaoCatalogo();

        tipo.setId(1L);

        tipo.setNome("Canalização");

        tipo.setDescricao("Descrição");

        assertEquals(
                1L,
                tipo.getId());

        assertEquals(
                "Canalização",
                tipo.getNome());

        assertEquals(
                "Descrição",
                tipo.getDescricao());
    }
}