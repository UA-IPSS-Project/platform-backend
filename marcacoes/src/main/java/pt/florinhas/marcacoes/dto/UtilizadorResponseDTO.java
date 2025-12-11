package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.Utilizador;

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

    // Construtor para converter de Utilizador
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
        return dto;
    }
}
