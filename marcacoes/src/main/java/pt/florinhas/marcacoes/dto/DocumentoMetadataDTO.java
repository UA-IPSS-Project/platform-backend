package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO com metadados completos de um documento armazenado no MinIO.
 */
public record DocumentoMetadataDTO(
    Long id,
    String nomeOriginal,
    String nomeArmazenado,
    String caminho,
    String tipo,
    Long tamanho,
    LocalDateTime uploadedEm,
    Long marcacaoId,
    String etag,
    String minioLastModified,
    Map<String, String> minioUserMetadata
) {}
