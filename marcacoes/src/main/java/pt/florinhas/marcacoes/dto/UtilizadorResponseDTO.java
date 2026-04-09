package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;

/**
 * DTO de resposta com os dados públicos de um Utilizador.
 *
 * Usos típicos:
 * - Devolver ao frontend informação pronta a consumir (strings formatadas),
 * sem expor a entidade JPA diretamente.
 * - Padronizar o contrato de saída em endpoints que consultam utilizadores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizadorResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String nif;
    private String telefone;
    private String dataNascimento;
    private String morada;
    private String codigoPostal;
    private String freguesia;
    private String profissao;
    private String localEmprego;
    private String moradaEmprego;
    private String telefoneEmprego;
    private boolean active;
    private String funcao;
    private boolean createdBySecretaria;

    /**
     * Construtor/factory estático para converter uma entidade Utilizador
     * no respetivo DTO de resposta.
     *
     * Responsabilidades:
     * - Extrair os campos de leitura da entidade de domínio.
     * - Tratar conversões simples (ex.: LocalDate -> String ISO).
     * - Evitar expor internamente objetos JPA ao exterior.
     *
     * param utilizador entidade de domínio a converter
     * return DTO preenchido com os dados do utilizador
     */
    public static UtilizadorResponseDTO fromUtilizador(Utilizador utilizador) {
        UtilizadorResponseDTO dto = new UtilizadorResponseDTO();
        dto.setId(utilizador.getId());
        dto.setNome(utilizador.getNome());
        dto.setEmail(utilizador.getEmail());
        dto.setNif(utilizador.getNif());
        dto.setTelefone(utilizador.getTelefone());
        dto.setDataNascimento(utilizador.getDataNasc() != null ? utilizador.getDataNasc().toString() : null);
        dto.setMorada(utilizador.getMorada());
        dto.setCodigoPostal(utilizador.getCodigoPostal());
        dto.setFreguesia(utilizador.getFreguesia());
        dto.setProfissao(utilizador.getProfissao());
        dto.setLocalEmprego(utilizador.getLocalEmprego());
        dto.setMoradaEmprego(utilizador.getMoradaEmprego());
        dto.setTelefoneEmprego(utilizador.getTelefoneEmprego());
        if (utilizador instanceof Funcionario) {
            Funcionario func = (Funcionario) utilizador;
            dto.setActive(func.isActivo());
            if (func.getTipo() != null) {
                dto.setFuncao(func.getTipo().toString());
            }
            // Se termsAcceptedAt é null, foi criado pela secretaria
            dto.setCreatedBySecretaria(utilizador.getTermsAcceptedAt() == null);
        } else if (utilizador instanceof Utente) {
            Utente utente = (Utente) utilizador;
            dto.setActive(utente.isActivo());
            dto.setFuncao("UTENTE");
            dto.setCreatedBySecretaria(utilizador.getTermsAcceptedAt() == null);
        } else {
            dto.setActive(true);
        }
        return dto;
    }
}
