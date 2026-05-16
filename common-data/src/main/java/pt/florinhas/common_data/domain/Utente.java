package pt.florinhas.common_data.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade JPA que representa um Utente (cliente/beneficiário do serviço).
 *
 * Herda de Utilizador (mesma PK e metadados base de identidade/conta),
 * acrescentando:
 *  - indicador de ativação/estado da conta (activo)
 *  - relação 1:N com as marcações de secretaria em que o utente é o destinatário
 */
@Entity
@Table(name = "utente")
@Getter
@Setter
@NoArgsConstructor
public class Utente extends Utilizador {

    /**
     * Estado lógico da conta do utente.
     * true  -> conta ativa, pode efetuar marcações e operações normais.
     * false -> conta desativada/suspensa; o serviço deve bloquear operações.
     * A validação de uso deste flag vive na camada de serviço.
     */
    private boolean activo;
}
