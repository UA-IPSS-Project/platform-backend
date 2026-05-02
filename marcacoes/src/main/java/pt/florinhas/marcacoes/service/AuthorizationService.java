package pt.florinhas.marcacoes.service;

import java.util.Arrays;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UtilizadorRepository utilizadorRepository;

    public Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Utilizador utilizador) {
            return utilizador.getId();
        }

        if (principal instanceof UserDetails userDetails) {
            var users = utilizadorRepository.findByEmail(userDetails.getUsername());
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
        }

        return null;
    }

    public boolean isAdmin() {
        return hasAnyRole("ROLE_SECRETARIA", "ROLE_BALNEARIO");
    }

    public boolean hasAnyRole(String... roles) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> Arrays.stream(roles).anyMatch(role -> role.equals(a.getAuthority())));
    }

    /**
     * Verifica se o utilizador atual tem permissão para aceder a um recurso.
     * Administradores (SECRETARIA/BALNEARIO) têm sempre acesso.
     * Utentes normais só têm acesso se forem o proprietário do recurso.
     *
     * @param ownerId ID do proprietário do recurso
     * @param resourceType tipo de recurso para a mensagem de erro
     * @throws AccessDeniedException se não tiver permissão
     */
    public void checkPermission(Long ownerId, String resourceType) {
        if (isAdmin()) return;
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(ownerId)) {
            throw new AccessDeniedException(
                String.format("Não tem permissão para %s.", resourceType));
        }
    }
}