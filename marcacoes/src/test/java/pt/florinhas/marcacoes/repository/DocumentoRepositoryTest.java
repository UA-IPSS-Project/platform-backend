package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class DocumentoRepositoryTest {

    @Test
    void classeDeveExistir() {

        assertNotNull(
                DocumentoRepository.class
        );
    }

    @Test
    void deveTerMetodoFindByNomeArmazenado() throws Exception {

        var method =
                DocumentoRepository.class.getMethod(
                        "findByNomeArmazenado",
                        String.class
                );

        assertNotNull(method);

        assertEquals(
                Optional.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByMarcacaoId() throws Exception {

        var method =
                DocumentoRepository.class.getMethod(
                        "findByMarcacaoId",
                        Long.class
                );

        assertNotNull(method);

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindAllByOrderByUploadedEmDesc() throws Exception {

        var method =
                DocumentoRepository.class.getMethod(
                        "findAllByOrderByUploadedEmDesc"
                );

        assertNotNull(method);

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByUploadedEmBetweenOrderByUploadedEmDesc() throws Exception {

        var method =
                DocumentoRepository.class.getMethod(
                        "findByUploadedEmBetweenOrderByUploadedEmDesc",
                        LocalDateTime.class,
                        LocalDateTime.class
                );

        assertNotNull(method);

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }

    @Test
    void deveTerMetodoFindByDataExpiracaoBeforeOrderByDataExpiracaoAsc() throws Exception {

        var method =
                DocumentoRepository.class.getMethod(
                        "findByDataExpiracaoBeforeOrderByDataExpiracaoAsc",
                        LocalDateTime.class
                );

        assertNotNull(method);

        assertEquals(
                List.class,
                method.getReturnType()
        );
    }
}