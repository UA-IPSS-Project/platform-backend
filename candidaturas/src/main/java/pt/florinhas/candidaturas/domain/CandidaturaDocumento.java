package pt.florinhas.candidaturas.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "candidatura_documentos")
public class CandidaturaDocumento {
    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed
    private String candidaturaId;

    private String minioKey;
    private String nomeOriginal;
    private String nomeArmazenado;
    private String tipo;
    private Long tamanho;
    private Instant uploadedEm;
    private Long uploadedPor;
}
