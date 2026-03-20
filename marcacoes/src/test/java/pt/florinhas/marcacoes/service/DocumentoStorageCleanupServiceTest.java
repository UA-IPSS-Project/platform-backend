package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;

@ExtendWith(MockitoExtension.class)
class DocumentoStorageCleanupServiceTest {

    @Mock
    private MinioClient minioClient;

    private DocumentoStorageCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        cleanupService = new DocumentoStorageCleanupService(minioClient, "marcacoes-test");
        cleanupService.init();
    }

    @Test
    void removerDoArmazenamento_DeveRemoverObjetoNoMinio_QuandoCaminhoValido() throws Exception {
        DocumentoStorageCleanupService.removerDoArmazenamento("2026/02/ficheiro.pdf", 7L);

        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());

        RemoveObjectArgs args = captor.getValue();
        assertEquals("marcacoes-test", args.bucket());
        assertEquals("2026/02/ficheiro.pdf", args.object());
    }

    @Test
    void removerDoArmazenamento_NaoDeveRemover_QuandoCaminhoVazio() throws Exception {
        DocumentoStorageCleanupService.removerDoArmazenamento("   ", 8L);

        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }
}
