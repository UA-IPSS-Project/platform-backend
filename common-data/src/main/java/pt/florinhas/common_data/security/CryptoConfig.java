package pt.florinhas.common_data.security;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import pt.florinhas.common_data.domain.Utilizador;

/**
 * Injeta o CryptoUtils na entidade Utilizador no arranque da aplicação.
 * Necessário porque entidades JPA não são Spring beans e não suportam @Autowired.
 */
@Configuration
public class CryptoConfig {

    private final CryptoUtils cryptoUtils;

    public CryptoConfig(CryptoUtils cryptoUtils) {
        this.cryptoUtils = cryptoUtils;
    }

    @PostConstruct
    public void init() {
        Utilizador.setCryptoUtils(cryptoUtils);
    }
}
