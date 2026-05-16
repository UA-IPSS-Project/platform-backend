package pt.florinhas.candidaturas.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;

import pt.florinhas.candidaturas.domain.FormDraft;
import pt.florinhas.candidaturas.domain.FormPage;

@Data
public class FormDraftResponse {
    private String id;
    private String formId;
    private String name;
    private List<FormPage> pages;
    private Long atualizadoPor;
    private String atualizadoPorNome;
    private Instant atualizadoEm;

    public static FormDraftResponse fromEntity(FormDraft draft) {
        if (draft == null) return null;
        FormDraftResponse dto = new FormDraftResponse();
        dto.setId(draft.getId());
        dto.setFormId(draft.getFormId());
        dto.setName(draft.getName());
        dto.setPages(draft.getPages());
        dto.setAtualizadoPor(draft.getAtualizadoPor());
        dto.setAtualizadoPorNome(draft.getAtualizadoPorNome());
        dto.setAtualizadoEm(draft.getAtualizadoEm());
        return dto;
    }
}
