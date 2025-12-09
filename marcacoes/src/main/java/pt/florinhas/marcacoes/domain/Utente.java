package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import java.util.Set;

@Entity
@Table(name = "utente")
@Getter
@Setter
@NoArgsConstructor
public class Utente extends Utilizador {

	@OneToMany(mappedBy = "utente", cascade = CascadeType.ALL)
    private Set<MarcacaoSecretaria> marcacoes;
}