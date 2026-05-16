package pt.florinhas.notificacoes.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilizadorRepository utilizadorRepository;
    private final CryptoUtils cryptoUtils;

    public CustomUserDetailsService(UtilizadorRepository utilizadorRepository, CryptoUtils cryptoUtils) {
        this.utilizadorRepository = utilizadorRepository;
        this.cryptoUtils = cryptoUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null) {
            throw new UsernameNotFoundException("Email is null");
        }
        String trimmedEmail = email.trim();

        var usersByEmail = utilizadorRepository.findByEmail(trimmedEmail);
        if (!usersByEmail.isEmpty()) {
            return usersByEmail.get(0);
        }

        var usersByNif = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(trimmedEmail));
        if (!usersByNif.isEmpty()) {
            return usersByNif.get(0);
        }

        throw new UsernameNotFoundException("Utilizador não encontrado: " + email);
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        var users = utilizadorRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Funcionário não encontrado com email: " + email);
        }
        return users.get(0);
    }

    public UserDetails loadUserByNif(String nif) throws UsernameNotFoundException {
        var users = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(nif));
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Utente não encontrado com NIF: " + nif);
        }
        return users.get(0);
    }
}
