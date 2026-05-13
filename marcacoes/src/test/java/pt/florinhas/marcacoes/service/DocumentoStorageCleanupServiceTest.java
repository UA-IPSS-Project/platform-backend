package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;

class DocumentoStorageCleanupServiceTest {

    private MinioClient minioClient;

    private DocumentoStorageCleanupService service;

    @BeforeEach
    void setup() {

        minioClient =
                mock(MinioClient.class);

        service =
                new DocumentoStorageCleanupService(
                        minioClient,
                        "test-bucket"
                );

        service.init();
    }

    @Test
    void removerDoArmazenamento_DeveIgnorarQuandoCaminhoNull() throws Exception {

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(
                                null,
                                1L
                        )
        );

        verify(minioClient, never())
                .removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void removerDoArmazenamento_DeveIgnorarQuandoCaminhoVazio() throws Exception {

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(
                                "   ",
                                1L
                        )
        );

        verify(minioClient, never())
                .removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void removerDoArmazenamento_DeveRemoverFicheiro() throws Exception {

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(
                                "pasta/teste.pdf",
                                1L
                        )
        );

        verify(minioClient, times(1))
                .removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void removerDoArmazenamento_DeveCapturarErroMinio() throws Exception {

        doThrow(new RuntimeException("Erro MinIO"))
                .when(minioClient)
                .removeObject(any(RemoveObjectArgs.class));

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(
                                "pasta/teste.pdf",
                                1L
                        )
        );

        verify(minioClient, times(1))
                .removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void removerDoArmazenamento_DeveIgnorarQuandoMinioNaoInicializado() {

        DocumentoStorageCleanupService outroService =
                new DocumentoStorageCleanupService(
                        null,
                        null
                );

        assertDoesNotThrow(() ->
                DocumentoStorageCleanupService
                        .removerDoArmazenamento(
                                "teste.pdf",
                                1L
                        )
        );
    }
}