package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoMetadataDTO {

    private Long id;
    private String nomeOriginal;
    private String nomeArmazenado;
    private String caminho;
    private String tipo;
    private Long tamanho;
    private LocalDateTime uploadedEm;
    private Long marcacaoId;
    private String etag;
    private String minioLastModified;
    private Map<String, String> minioUserMetadata;
    private Integer sequencia;
}
