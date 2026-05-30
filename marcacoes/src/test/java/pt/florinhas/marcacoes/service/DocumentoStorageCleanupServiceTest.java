package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.minio.MinioClient;
import pt.florinhas.marcacoes.service.DocumentoStorageCleanupService;

class DocumentoStorageCleanupServiceTest {

    private MinioClient minioClient;

    private DocumentoStorageCleanupService service;

    @BeforeEach
    void setUp() {

        minioClient = org.mockito.Mockito.mock(MinioClient.class);

        service = new DocumentoStorageCleanupService(minioClient, "marcacoes");

        service.init();
    }

    @Test
    void removerDoArmazenamento_DeveIgnorarCaminhoNull() {

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(null, 1L));
    }

    @Test
    void removerDoArmazenamento_DeveIgnorarCaminhoVazio() {

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(" ", 1L));
    }

    @Test
    void removerDoArmazenamento_NaoDeveLancarErro() {

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(
                                "teste.pdf",
                                1L));
    }
}