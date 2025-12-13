package pt.florinhas.marcacoes.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pt.florinhas.marcacoes.repository.UtilizadorRepository;

/**
 * Implementação de UserDetailsService que integra a autenticação
 * do Spring Security com o modelo de domínio da aplicação.
 *
 * Características:
 *  - @Primary: quando existirem múltiplos UserDetailsService no contexto,
 *    este é o escolhido por defeito pelo Spring Security.
 *  - Suporta lookup por email (funcionários) e por NIF (utentes), permitindo
 *    que o "username" recebido na autenticação seja um ou outro.
 *
 * Exceções:
 *  - Lança UsernameNotFoundException quando não encontra o utilizador,
 *    que é mapeada internamente pelo Spring Security para falha de autenticação.
 */
@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilizadorRepository utilizadorRepository;

    public CustomUserDetailsService(UtilizadorRepository utilizadorRepository) {
        this.utilizadorRepository = utilizadorRepository;
    }

    /**
     * Carrega um utilizador pelo "username" fornecido ao Spring Security.
     *
     * Estratégia:
     *  1) Tenta encontrar por email (caso comum para Funcionários).
     *  2) Caso falhe, tenta por NIF (caso comum para Utentes).
     *
     * param email valor do identificador fornecido (pode ser email ou NIF)
     * return UserDetails do utilizador encontrado
     * throws UsernameNotFoundException quando não encontra nem por email nem por NIF
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tenta por email primeiro (funcionários), depois por NIF (utentes)
        return utilizadorRepository.findByEmail(email)
                .or(() -> utilizadorRepository.findByNif(email))
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));
    }
    
    /**
     * Carrega um utilizador especificamente por email.
     * Útil para fluxos onde o identificador deve ser explicitamente um email
     * (ex.: backoffice de funcionários).
     *
     * param email email do utilizador
     * return UserDetails correspondente
     * throws UsernameNotFoundException se não existir utilizador com esse email
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return utilizadorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionário não encontrado com email: " + email));
    }
    
    /**
     * Carrega um utilizador especificamente por NIF.
     * Útil para fluxos onde o identificador deve ser explicitamente um NIF
     * (ex.: autenticação de utentes).
     *
     * param nif NIF do utilizador (9 dígitos)
     * return UserDetails correspondente
     * throws UsernameNotFoundException se não existir utilizador com esse NIF
     */
    public UserDetails loadUserByNif(String nif) throws UsernameNotFoundException {
        return utilizadorRepository.findByNif(nif)
                .orElseThrow(() -> new UsernameNotFoundException("Utente não encontrado com NIF: " + nif));
    }
}
