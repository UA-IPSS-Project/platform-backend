package pt.florinhas.marcacoes.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

/**
 * Chama a Keycloak Admin REST API para criar/gerir utilizadores.
 * Usa client credentials (florinhas-gateway) para obter um access token de admin.
 */
@Service
@Slf4j
public class KeycloakAdminClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.admin.url:http://localhost:8180}")
    private String keycloakUrl;

    @Value("${keycloak.admin.realm:florinhas}")
    private String realm;

    @Value("${keycloak.admin.user:admin}")
    private String adminUser;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUser);
        body.add("password", adminPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                tokenUrl, new HttpEntity<>(body, headers), Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Failed to obtain Keycloak admin token via master realm");
        }
        return (String) response.get("access_token");
    }

    /**
     * Cria um utilizador no Keycloak e atribui a role indicada.
     */
    public String criarUtilizador(String email, String nome, String role, String temporaryPassword) {
        String existingId = getUserIdByEmail(email);
        if (existingId != null) {
            log.info("User {} already exists in Keycloak (id: {}). Skipping creation.", email, existingId);
            return existingId;
        }

        try {
            String token = getAdminToken();
            String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";
            // ... rest of the logic

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String[] nameParts = nome.trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            Map<String, Object> userRep = Map.of(
                    "username", email,
                    "email", email,
                    "firstName", firstName,
                    "lastName", lastName,
                    "enabled", true,
                    "credentials", List.of(Map.of(
                            "type", "password",
                            "value", temporaryPassword,
                            "temporary", true)));

            var response = restTemplate.postForEntity(
                    usersUrl, new HttpEntity<>(userRep, headers), Void.class);

            // Extract user ID from Location header
            String location = response.getHeaders().getFirst("Location");
            if (location == null) throw new IllegalStateException("No Location header in Keycloak response");
            String userId = location.substring(location.lastIndexOf('/') + 1);

            // Assign realm role
            assignRole(token, userId, role);

            log.info("Keycloak user created: {} ({})", email, userId);
            return userId;

        } catch (Exception e) {
            log.error("Failed to create Keycloak user for {}: {}", email, e.getMessage());
            // Non-fatal: user exists in local DB, can be synced later
            return null;
        }
    }

    public String getUserIdByEmail(String email) {
        try {
            String token = getAdminToken();
            String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email + "&exact=true";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users = restTemplate.exchange(
                    searchUrl, org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(headers), List.class).getBody();

            if (users != null && !users.isEmpty()) {
                return (String) users.get(0).get("id");
            }
        } catch (Exception e) {
            log.warn("Failed to search user by email {}: {}", email, e.getMessage());
        }
        return null;
    }

    /**
     * Atualiza a password de um utilizador no Keycloak.
     */
    public void atualizarPassword(String email, String novaPassword) {
        String userId = getUserIdByEmail(email);
        if (userId == null) {
            throw new IllegalStateException("Utilizador não encontrado no Keycloak: " + email);
        }

        try {
            String token = getAdminToken();
            String resetPasswordUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> credentialRep = Map.of(
                    "type", "password",
                    "value", novaPassword,
                    "temporary", false);

            restTemplate.put(resetPasswordUrl, new HttpEntity<>(credentialRep, headers));
            log.info("Password updated successfully in Keycloak for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to update password in Keycloak for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Erro ao atualizar password no Keycloak: " + e.getMessage(), e);
        }
    }

    private void assignRole(String token, String userId, String roleName) {
        try {
            // Get role representation
            String roleUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            @SuppressWarnings("unchecked")
            Map<String, Object> roleRep = restTemplate.exchange(
                    roleUrl, org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class).getBody();

            if (roleRep == null) return;

            // Assign role to user
            String assignUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(assignUrl, new HttpEntity<>(List.of(roleRep), headers), Void.class);
        } catch (Exception e) {
            log.warn("Failed to assign role {} to Keycloak user {}: {}", roleName, userId, e.getMessage());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(@JsonProperty("access_token") String accessToken) {}
}
