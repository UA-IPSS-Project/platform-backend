package pt.florinhas.candidaturas.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "candidatura_secretary_drafts")
public class CandidaturaSecretaryDraft {
    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed(unique = true)
    private String candidaturaId;

    private Map<String, Object> respostas;

    private Long atualizadoPor;
    private String atualizadoPorNome;
    private Instant atualizadoEm;
}
