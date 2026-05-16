package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class AssuntoRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(AssuntoRepository.class);
    }

    @Test
    void deveTerMetodoFindByAtivoTrue() throws Exception {

        var method =
                AssuntoRepository.class.getMethod(
                        "findByAtivoTrue"
                );

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByNome() throws Exception {

        var method =
                AssuntoRepository.class.getMethod(
                        "findByNome",
                        String.class
                );

        assertEquals(
                Optional.class,
                method.getReturnType()
        );
    }
}