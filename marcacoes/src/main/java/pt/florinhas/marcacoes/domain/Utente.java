package pt.florinhas.marcacoes.domain;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "utente")
@Getter
@Setter
@NoArgsConstructor
public class Utente extends Utilizador {

    private boolean activo;

	@OneToMany(mappedBy = "utente", cascade = CascadeType.ALL)
    private Set<MarcacaoSecretaria> marcacoes;
}