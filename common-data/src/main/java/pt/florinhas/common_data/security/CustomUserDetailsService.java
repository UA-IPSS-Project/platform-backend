package pt.florinhas.common_data.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pt.florinhas.common_data.repository.UtilizadorRepository;

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
            throw new UsernameNotFoundException("Utilizador não encontrado");
        }
        String trimmedEmail = email.trim();

        // 1) Tenta por email primeiro (funcionários)
        var usersByEmail = utilizadorRepository.findByEmail(trimmedEmail);
        if (!usersByEmail.isEmpty()) {
            return usersByEmail.get(0);
        }

        // 2) Depois por NIF (utentes) - devolve Lista
        var usersByNif = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(trimmedEmail));
        if (!usersByNif.isEmpty()) {
            return usersByNif.get(0);
        }

        throw new UsernameNotFoundException("Utilizador não encontrado");
    }
}
