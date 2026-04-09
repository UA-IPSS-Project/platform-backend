package pt.florinhas.common_data.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Entidade JPA que representa um Funcionário.
 *
 * Herda de Utilizador (estratégia JOINED por associação
 * com @PrimaryKeyJoinColumn),
 * acrescentando:
 * - o tipo de funcionário (SECRETARIA, BALNEARIO, ...)
 * - as valências a que pertence (Many-to-Many)
 */
@Entity
@Table(name = "Funcionario")
@PrimaryKeyJoinColumn(name = "Utilizador_id")
@Data
@EqualsAndHashCode(callSuper = true)
public class Funcionario extends Utilizador {

    /**
     * Tipo de funcionário, armazenado como texto (EnumType.STRING).
     *
     * Motivo para STRING:
     * - evita problemas de migração quando a ordem do enum muda
     * - torna os dados mais legíveis na base de dados
     */
    @Enumerated(EnumType.STRING)
    private FuncionarioTipo tipo; // SECRETARIA, BALNEARIO

    /**
     * Conjunto de valências a que o funcionário pertence.
     *
     * Mapeamento Many-to-Many via tabela de junção "pertence":
     * - FK para Funcionário: 'funcionario_id'
     * - FK para Valência: 'valencia_id'
     */
    @ManyToMany
    @JoinTable(name = "pertence", joinColumns = @JoinColumn(name = "funcionario_id"), inverseJoinColumns = @JoinColumn(name = "valencia_id"))
    private Set<Valencia> valencias;

    /**
     * Estado da conta do funcionário.
     * false = Pendente de aprovação.
     * true = Ativo.
     */
    private boolean activo;

    @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.tipo == null) {
            return List.of(
                new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
        }
        return List.of(
            new SimpleGrantedAuthority("ROLE_" + this.tipo.name()),
            new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
    }
}
