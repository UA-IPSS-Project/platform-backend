package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.Documento;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {

    private Long id;
    private String nomeOriginal;
    private String tipo;
    private Long tamanho;
    private LocalDateTime uploadedEm;
    private Long marcacaoId;
    private String utenteNome;
    private String utenteNif;
    private Integer sequencia;
    private String finalidade;

    public static DocumentoDTO fromDocumento(Documento documento) {
        String utenteNome = null;
        String utenteNif = null;
        if (documento.getMarcacao() != null && documento.getMarcacao().getMarcacaoSecretaria() != null && documento.getMarcacao().getMarcacaoSecretaria().getUtente() != null) {
            utenteNome = documento.getMarcacao().getMarcacaoSecretaria().getUtente().getNome();
            utenteNif = documento.getMarcacao().getMarcacaoSecretaria().getUtente().getNif();
        }
        return new DocumentoDTO(documento.getId(), documento.getNomeOriginal(), documento.getTipo(), documento.getTamanho(), documento.getUploadedEm(), documento.getMarcacao().getId(), utenteNome, utenteNif, documento.getSequencia(), documento.getFinalidade());
    }

    // Record-style accessors for backward compatibility with existing callers
    public Long id() { return id; }
    public String nomeOriginal() { return nomeOriginal; }
    public String tipo() { return tipo; }
    public Long tamanho() { return tamanho; }
    public LocalDateTime uploadedEm() { return uploadedEm; }
    public Long marcacaoId() { return marcacaoId; }
    public String utenteNome() { return utenteNome; }
    public String utenteNif() { return utenteNif; }
    public Integer sequencia() { return sequencia; }
    public String finalidade() { return finalidade; }
}
