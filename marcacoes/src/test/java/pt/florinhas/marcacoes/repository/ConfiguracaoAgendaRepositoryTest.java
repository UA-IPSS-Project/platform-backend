package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ConfiguracaoAgendaRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(
                ConfiguracaoAgendaRepository.class
        );
    }

    @Test
    void deveTerMetodoFindByTipo() throws Exception {

        var method =
                ConfiguracaoAgendaRepository.class.getMethod(
                        "findByTipo",
                        String.class
                );

        assertNotNull(method);

        assertEquals(
                Optional.class,
                method.getReturnType()
        );
    }
}