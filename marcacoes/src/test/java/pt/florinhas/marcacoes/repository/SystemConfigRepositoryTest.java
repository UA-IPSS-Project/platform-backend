package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class SystemConfigRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(
                SystemConfigRepository.class
        );
    }

    @Test
    void deveTerMetodoFindByConfigKey() throws Exception {

        var method =
                SystemConfigRepository.class.getMethod(
                        "findByConfigKey",
                        String.class
                );

        assertNotNull(method);

        assertEquals(
                Optional.class,
                method.getReturnType()
        );
    }
}