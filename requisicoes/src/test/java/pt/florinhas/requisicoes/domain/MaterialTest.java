package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MaterialTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        Material material = new Material();

        material.setId(1L);
        material.setNome("Caneta");
        material.setCategoria("ESCRITA");
        material.setAtributo("Cor");
        material.setValorAtributo("Azul");

        assertEquals(1L, material.getId());
        assertEquals("Caneta", material.getNome());
        assertEquals("ESCRITA", material.getCategoria());
        assertEquals("Cor", material.getAtributo());
        assertEquals("Azul", material.getValorAtributo());
    }
}