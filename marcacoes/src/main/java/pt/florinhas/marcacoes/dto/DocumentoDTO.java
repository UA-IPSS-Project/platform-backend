package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import pt.florinhas.marcacoes.domain.Documento;

/**
 * DTO para transferência de dados de documentos.
 * 
 * Usado para retornar informações sobre documentos anexados a marcações,
 * sem expor detalhes internos como o caminho completo do sistema de ficheiros.
 */
public record DocumentoDTO(
    Long id,
    String nomeOriginal,
    String tipo,
    Long tamanho,
    LocalDateTime uploadedEm,
    Long marcacaoId,
    String utenteNome,
    String utenteNif,
    Integer sequencia
) {
    /**
     * Converte uma entidade Documento em DTO.
     * 
     * @param documento entidade de domínio
     * @return DTO com dados públicos do documento
     */
    public static DocumentoDTO fromDocumento(Documento documento) {
        String utenteNome = null;
        String utenteNif = null;

        if (documento.getMarcacao() != null
            && documento.getMarcacao().getMarcacaoSecretaria() != null
            && documento.getMarcacao().getMarcacaoSecretaria().getUtente() != null) {
            utenteNome = documento.getMarcacao().getMarcacaoSecretaria().getUtente().getNome();
            utenteNif = documento.getMarcacao().getMarcacaoSecretaria().getUtente().getNif();
        }

        return new DocumentoDTO(
            documento.getId(),
            documento.getNomeOriginal(),
            documento.getTipo(),
            documento.getTamanho(),
            documento.getUploadedEm(),
            documento.getMarcacao().getId(),
            utenteNome,
            utenteNif,
            documento.getSequencia()
        );
    }
}
