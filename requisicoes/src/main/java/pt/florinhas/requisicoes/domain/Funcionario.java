package pt.florinhas.requisicoes.domain;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "Funcionario")
@PrimaryKeyJoinColumn(name = "Utilizador_id")
@Data
@EqualsAndHashCode(callSuper = true)
public class Funcionario extends Utilizador {

    @Enumerated(EnumType.STRING)
    private FuncionarioTipo tipo;

    @ManyToMany
    @JoinTable(name = "pertence", joinColumns = @JoinColumn(name = "funcionario_id"), inverseJoinColumns = @JoinColumn(name = "valencia_id"))
    private Set<Valencia> valencias;

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
