package pt.florinhas.api_gateway.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import pt.florinhas.api_gateway.dto.AuthResponse;
import pt.florinhas.api_gateway.security.JwtService;
import pt.florinhas.api_gateway.security.JwtService.AuthUserClaims;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RestClient restClient;
    private final JwtService jwtService;

    @Value("${auth.marcacoes-base-url:http://localhost:8081}")
    private String marcacoesBaseUrl;

    public AuthController(RestClient restClient, JwtService jwtService) {
        this.restClient = restClient;
        this.jwtService = jwtService;
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<?> loginFuncionario(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/login/funcionario", payload);
    }

    @PostMapping("/login/utente")
    public ResponseEntity<?> loginUtente(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/login/utente", payload);
    }

    @PostMapping("/register/utente")
    public ResponseEntity<?> registerUtente(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/register/utente", payload);
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<?> registerFuncionario(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/register/funcionario", payload);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.status(401).build();
        }

        try {
            var claims = jwtService.parseClaims(token);
            AuthResponse response = new AuthResponse(
                    claims.get("userId", Number.class).longValue(),
                    claims.get("email", String.class),
                    claims.get("nome", String.class),
                    claims.get("role", String.class),
                    claims.get("nif", String.class),
                    claims.get("telefone", String.class),
                    claims.getExpiration().getTime(),
                    true);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Authenticated-User", required = false) String user,
            @RequestHeader(value = "X-Authenticated-Roles", required = false) String roles,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) String userId) {

        if (!StringUtils.hasText(user)) {
            return ResponseEntity.status(401).build();
        }

        try {
            HttpStatusCode status = restClient.put()
                    .uri(marcacoesBaseUrl + "/api/auth/password")
                    .header("X-Authenticated-User", user)
                    .header("X-Authenticated-Roles", roles == null ? "" : roles)
                    .header("X-Authenticated-User-Id", userId == null ? "" : userId)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            return ResponseEntity.status(status).build();
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }

    private ResponseEntity<?> proxyLoginOrRegister(String path, Map<String, Object> payload) {
        try {
            AuthResponse downstream = restClient.post()
                    .uri(marcacoesBaseUrl + path)
                    .body(payload)
                    .retrieve()
                    .body(AuthResponse.class);

            if (downstream == null) {
                return ResponseEntity.status(502).body(Map.of("message", "Resposta inválida do serviço de autenticação"));
            }

            String roleName = downstream.role() == null ? "UTENTE" : downstream.role();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }

            String token = jwtService.generateToken(new AuthUserClaims(
                    downstream.id(),
                    downstream.email(),
                    downstream.nome(),
                    downstream.role(),
                    downstream.nif(),
                    downstream.telefone(),
                    List.of(new SimpleGrantedAuthority(roleName))));

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(-1)
                    .sameSite("Lax")
                    .build();

            AuthResponse response = new AuthResponse(
                    downstream.id(),
                    downstream.email(),
                    downstream.nome(),
                    downstream.role(),
                    downstream.nif(),
                    downstream.telefone(),
                    System.currentTimeMillis() + jwtService.getJwtExpiration(),
                    downstream.active());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
