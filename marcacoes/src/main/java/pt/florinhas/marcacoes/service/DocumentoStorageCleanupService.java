package pt.florinhas.marcacoes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de limpeza de ficheiros físicos no MinIO quando documentos são removidos.
 */
@Service
@Slf4j
public class DocumentoStorageCleanupService {

    private static MinioClient minioClientStatic;
    private static String bucketNameStatic;

    private final MinioClient minioClient;
    private final String bucketName;

    public DocumentoStorageCleanupService(
        MinioClient minioClient,
        @Value("${minio.bucket:marcacoes}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    private static void setStaticFields(MinioClient minioClient, String bucketName) {
        minioClientStatic = minioClient;
        bucketNameStatic = bucketName;
    }

    @PostConstruct
    void init() {
        setStaticFields(minioClient, bucketName);
    }

    public static void removerDoArmazenamento(String caminho, Long documentoId) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }

        if (minioClientStatic == null || bucketNameStatic == null) {
            log.warn("Cleanup do documento {} ignorado: MinIO ainda não inicializado", documentoId);
            return;
        }

        try {
            minioClientStatic.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketNameStatic)
                    .object(caminho)
                    .build()
            );
            log.info("Ficheiro removido do MinIO: {} (documento={})", caminho, documentoId);
        } catch (Exception e) {
            log.error("Erro ao remover ficheiro do MinIO: {} (documento={})", caminho, documentoId, e);
        }
    }
}