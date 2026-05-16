package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA que representa um item do armazém do Balneário.
 * Cada item pertence a uma categoria (DETERGENTES, HIGIENE, CALCADO)
 * e tem uma quantidade atual e um valor mínimo para alertas de stock.
 */
@Entity
@Table(name = "Item_Armazem", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"categoria", "nome"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemArmazem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Categoria do item: DETERGENTES, HIGIENE, CALCADO
     */
    @Column(name = "categoria", length = 50, nullable = false)
    private String categoria;

    /**
     * Nome do produto ou tamanho do calçado (ex: "Champô", "42")
     */
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    /**
     * Quantidade atual em stock.
     */
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 0;

    /**
     * Quantidade mínima para alerta de stock baixo.
     */
    @Column(name = "quantidade_minima", nullable = false)
    private Integer quantidadeMinima = 0;

    /**
     * Unidade de medida: L, un, pk, rolos, pares
     */
    @Column(name = "unidade", length = 20, nullable = false)
    private String unidade = "un";

    /**
     * Marca ou fabricante do produto.
     */
    @Column(name = "marca", length = 100)
    private String marca;

    /**
     * Tamanho específico (ex: "L", "XL", "M") para vestuário.
     */
    @Column(name = "tamanho", length = 20)
    private String tamanho;

    /**
     * Volume ou peso (ex: 1.5, 500.0) para detergentes ou higiene.
     */
    @Column(name = "volume")
    private Double volume;

    /**
     * Descrição adicional do item.
     */
    @Column(name = "descricao", length = 255)
    private String descricao;
}