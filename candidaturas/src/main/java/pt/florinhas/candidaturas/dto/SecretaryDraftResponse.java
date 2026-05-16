package pt.florinhas.candidaturas.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Data;

import pt.florinhas.candidaturas.domain.CandidaturaSecretaryDraft;

@Data
public class SecretaryDraftResponse {
    private String id;
    private String candidaturaId;
    private Map<String, Object> respostas;
    private Long atualizadoPor;
    private String atualizadoPorNome;
    private Instant atualizadoEm;

    public static SecretaryDraftResponse fromEntity(CandidaturaSecretaryDraft draft) {
        if (draft == null) return null;
        SecretaryDraftResponse dto = new SecretaryDraftResponse();
        dto.setId(draft.getId());
        dto.setCandidaturaId(draft.getCandidaturaId());
        dto.setRespostas(draft.getRespostas());
        dto.setAtualizadoPor(draft.getAtualizadoPor());
        dto.setAtualizadoPorNome(draft.getAtualizadoPorNome());
        dto.setAtualizadoEm(draft.getAtualizadoEm());
        return dto;
    }
}
