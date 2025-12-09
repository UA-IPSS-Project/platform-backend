package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Entity
@Table(name = "Funcionario")
@PrimaryKeyJoinColumn(name = "Utilizador_id")
@Data
@EqualsAndHashCode(callSuper = true)
public class Funcionario extends Utilizador {

    
	@Enumerated(EnumType.STRING)
	private FuncionarioTipo tipo; // SECRETARIA, BALNEARIO
    
    
    @ManyToMany
    @JoinTable(name = "pertence",joinColumns = @JoinColumn(name = "funcionario_id"),inverseJoinColumns = @JoinColumn(name = "valencia_id"))
    private Set<Valencia> valencias;

}