package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ItemArmazemRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(
                ItemArmazemRepository.class
        );
    }

    @Test
    void deveTerMetodoFindByCategoria() throws Exception {

        var method =
                ItemArmazemRepository.class.getMethod(
                        "findByCategoria",
                        String.class
                );

        assertNotNull(method);

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByCategoriaAndNome() throws Exception {

        var method =
                ItemArmazemRepository.class.getMethod(
                        "findByCategoriaAndNome",
                        String.class,
                        String.class
                );

        assertNotNull(method);

        assertEquals(
                Optional.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindAllByOrderByCategoriaAscNomeAsc() throws Exception {

        var method =
                ItemArmazemRepository.class.getMethod(
                        "findAllByOrderByCategoriaAscNomeAsc"
                );

        assertNotNull(method);

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }
}