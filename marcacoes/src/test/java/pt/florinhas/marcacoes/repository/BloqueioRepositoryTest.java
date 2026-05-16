package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class BloqueioRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(BloqueioRepository.class);
    }

    @Test
    void deveTerMetodoFindByData() throws Exception {

        var method =
                BloqueioRepository.class.getMethod(
                        "findByData",
                        LocalDate.class
                );

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByDataBetween() throws Exception {

        var method =
                BloqueioRepository.class.getMethod(
                        "findByDataBetween",
                        LocalDate.class,
                        LocalDate.class
                );

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoExisteSobreposicao() throws Exception {

        var method =
                BloqueioRepository.class.getMethod(
                        "existeSobreposicao",
                        LocalDate.class,
                        LocalTime.class,
                        LocalTime.class
                );

        assertEquals(
                boolean.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoCountConflictingWithLock() throws Exception {

        var method =
                BloqueioRepository.class.getMethod(
                        "countConflictingWithLock",
                        LocalDate.class,
                        LocalTime.class,
                        LocalTime.class
                );

        assertEquals(
                long.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByTipo() throws Exception {

        var method =
                BloqueioRepository.class.getMethod(
                        "findByTipo",
                        String.class
                );

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }
}