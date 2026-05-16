package pt.florinhas.candidaturas.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

@Getter
@Setter
@Document(collection = "form_drafts")
public class FormDraft {
    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed(unique = true)
    private String formId;

    private String name;

    private List<FormPage> pages;

    private Long atualizadoPor;
    private String atualizadoPorNome;
    private Instant atualizadoEm;
}
