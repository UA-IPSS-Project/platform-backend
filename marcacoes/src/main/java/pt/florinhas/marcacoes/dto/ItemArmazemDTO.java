package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para comunicação de itens do armazém com o frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemArmazemDTO {

    private Long id;
    private String categoria;
    private String nome;
    private Integer quantidade;
    private Integer quantidadeMinima;
    private String unidade;
    private String marca;
    private String tamanho;
    private Double volume;
    private String descricao;

    /**
     * Estado calculado: "OK" se quantidade >= quantidadeMinima, "BAIXO" caso contrário.
     */
    private String estado;
}
