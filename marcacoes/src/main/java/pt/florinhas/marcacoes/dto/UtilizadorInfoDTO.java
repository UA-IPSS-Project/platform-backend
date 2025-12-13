package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO para atualização/edição de informação de perfil de um Utilizador.
 *
 * Usos típicos:
 *  - Receber do frontend os novos dados de perfil (contactos, morada, profissão, etc.)
 *  - Servir de payload para o serviço que valida e persiste as alterações
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizadorInfoDTO {
    private String nome;
    private String email;
    private String telefone;
    private String dataNasc; // formato: YYYY-MM-DD
    private String morada;
    private String codigoPostal;
    private String freguesia;
    private String telefoneEmprego;
    private String localEmprego;
    private String moradaEmprego;
    private String profissao;
}
