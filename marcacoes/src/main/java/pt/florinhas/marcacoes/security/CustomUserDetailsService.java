package pt.florinhas.marcacoes.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pt.florinhas.marcacoes.repository.UtilizadorRepository;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilizadorRepository utilizadorRepository;

    public CustomUserDetailsService(UtilizadorRepository utilizadorRepository) {
        this.utilizadorRepository = utilizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tenta por email primeiro (funcionários), depois por NIF (utentes)
        return utilizadorRepository.findByEmail(email)
                .or(() -> utilizadorRepository.findByNif(email))
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));
    }
    
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return utilizadorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionário não encontrado com email: " + email));
    }
    
    public UserDetails loadUserByNif(String nif) throws UsernameNotFoundException {
        return utilizadorRepository.findByNif(nif)
                .orElseThrow(() -> new UsernameNotFoundException("Utente não encontrado com NIF: " + nif));
    }
}
