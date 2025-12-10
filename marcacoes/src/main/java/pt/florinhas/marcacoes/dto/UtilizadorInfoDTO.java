package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
