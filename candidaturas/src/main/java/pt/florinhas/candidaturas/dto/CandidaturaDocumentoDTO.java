package pt.florinhas.candidaturas.dto;

import java.time.Instant;

import lombok.Data;

import pt.florinhas.candidaturas.domain.CandidaturaDocumento;

@Data
public class CandidaturaDocumentoDTO {
    private String id;
    private String candidaturaId;
    private String nomeOriginal;
    private String tipo;
    private Long tamanho;
    private Instant uploadedEm;
    private Long uploadedPor;

    public static CandidaturaDocumentoDTO fromEntity(CandidaturaDocumento doc) {
        if (doc == null) return null;
        CandidaturaDocumentoDTO dto = new CandidaturaDocumentoDTO();
        dto.setId(doc.getId());
        dto.setCandidaturaId(doc.getCandidaturaId());
        dto.setNomeOriginal(doc.getNomeOriginal());
        dto.setTipo(doc.getTipo());
        dto.setTamanho(doc.getTamanho());
        dto.setUploadedEm(doc.getUploadedEm());
        dto.setUploadedPor(doc.getUploadedPor());
        return dto;
    }
}
