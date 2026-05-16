package pt.florinhas.requisicoes.security;

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
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Username is null or blank");
        }
        String trimmed = email.trim();

        var usersByEmail = utilizadorRepository.findByEmail(trimmed);
        if (!usersByEmail.isEmpty()) {
            return usersByEmail.get(0);
        }

        var usersByNif = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(trimmed));
        if (!usersByNif.isEmpty()) {
            return usersByNif.get(0);
        }

        throw new UsernameNotFoundException("Utilizador não encontrado: " + email);
    }
}
