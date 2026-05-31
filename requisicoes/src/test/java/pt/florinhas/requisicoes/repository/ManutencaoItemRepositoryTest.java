package pt.florinhas.requisicoes.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ManutencaoItemRepositoryTest {

    @Test
    void methods_DeveExistir() {

        List<String> methods =
                List.of(
                        "findByCategoria",
                        "findAllByOrderByCategoriaAscEspacoAsc");

        assertEquals(
                2,
                methods.size());

        assertTrue(
                methods.contains(
                        "findByCategoria"));
    }
}