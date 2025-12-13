package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

/**
 * Entidade JPA que representa uma Valência/Serviço.
 *
 * Uma valência identifica uma área, serviço ou competência
 * (ex.: atendimento administrativo, apoio específico, etc.)
 * à qual um ou mais funcionários podem estar associados.
 *
 * É usada para:
 *  - classificar funcionários,
 *  - filtrar agendas/atendimentos,
 *  - aplicar regras de atribuição de marcações.
 */
@Entity
@Table(name = "valencia")
@Getter
@Setter
@NoArgsConstructor
public class Valencia {


    // Chave primária autogerada (IDENTITY).

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome da valência.
     * Campo obrigatório e limitado a 100 caracteres.
     */
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /**
     * Descrição opcional da valência.
     * Pode conter detalhes adicionais sobre o serviço prestado.
     */
    @Column(name = "descricao", length = 255)
    private String descricao;

    /**
     * Funcionários associados a esta valência.
     *
     * Lado inverso da relação Many-to-Many definida em {@link Funcionario#valencias}.
     * 'mappedBy' indica que a tabela de junção é definida do lado de Funcionario.
     *
     * Considerações:
     *  - A coleção é LAZY por defeito.
     *  - A gestão de associações (adicionar/remover funcionários)
     *    deve ser feita de forma consistente na camada de serviço.
     */
    @ManyToMany(mappedBy = "valencias")
    private Set<Funcionario> funcionarios;
}
