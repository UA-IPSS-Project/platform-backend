package pt.florinhas.candidaturas.dto;

import java.time.Instant;
import java.util.Map;
import lombok.Data;
import pt.florinhas.candidaturas.domain.Candidatura;
import pt.florinhas.candidaturas.domain.CandidaturaEstado;

@Data
public class CandidaturaResponse {
    private String id;
    private String formId;
    private String nif;
    private String nome;
    private Map<String, Object> respostas;
    private CandidaturaEstado estado;
    private Long criadoPor;
    private Instant criadoEm;
    private Boolean assinado;
    private Integer ranking;
    private Long utenteId;
    private Long atualizadoPor;
    private Instant atualizadoEm;

    public static CandidaturaResponse fromEntity(Candidatura candidatura) {
        if (candidatura == null) return null;
        CandidaturaResponse dto = new CandidaturaResponse();
        dto.setId(candidatura.getId());
        dto.setFormId(candidatura.getFormId());
        dto.setNif(candidatura.getNif());
        dto.setNome(candidatura.getNome());
        dto.setRespostas(candidatura.getRespostas());
        dto.setEstado(candidatura.getEstado());
        dto.setCriadoPor(candidatura.getCriadoPor());
        dto.setCriadoEm(candidatura.getCriadoEm());
        dto.setAssinado(candidatura.getAssinado());
        dto.setRanking(candidatura.getRanking());
        dto.setUtenteId(candidatura.getUtenteId());
        dto.setAtualizadoPor(candidatura.getAtualizadoPor());
        dto.setAtualizadoEm(candidatura.getAtualizadoEm());
        return dto;
    }
}
