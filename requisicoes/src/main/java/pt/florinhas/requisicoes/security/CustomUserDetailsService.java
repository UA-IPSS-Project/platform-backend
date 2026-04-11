package pt.florinhas.requisicoes.security;

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

    public CustomUserDetailsService(UtilizadorRepository utilizadorRepository) {
        this.utilizadorRepository = utilizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var usersByEmail = utilizadorRepository.findByEmail(email);
        if (!usersByEmail.isEmpty()) {
            return usersByEmail.get(0);
        }

        var usersByNif = utilizadorRepository.findByNif(email);
        if (!usersByNif.isEmpty()) {
            return usersByNif.get(0);
        }

        throw new UsernameNotFoundException("Utilizador não encontrado: " + email);
    }
}
