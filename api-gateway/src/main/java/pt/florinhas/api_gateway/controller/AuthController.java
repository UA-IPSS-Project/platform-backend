package pt.florinhas.api_gateway.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.springframework.http.server.reactive.ServerHttpRequest;
import pt.florinhas.api_gateway.dto.AuthResponse;
import pt.florinhas.api_gateway.security.JwtService;
import pt.florinhas.api_gateway.security.JwtService.AuthUserClaims;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final WebClient webClient;
    private final JwtService jwtService;

    @Value("${auth.marcacoes-base-url:http://localhost:8081}")
    private String marcacoesBaseUrl;

    public AuthController(WebClient webClient, JwtService jwtService) {
        this.webClient = webClient;
        this.jwtService = jwtService;
    }

    @PostMapping("/login/funcionario")
    public Mono<ResponseEntity<Object>> loginFuncionario(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/login/funcionario", payload);
    }

    @PostMapping("/login/utente")
    public Mono<ResponseEntity<Object>> loginUtente(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/login/utente", payload);
    }

    @PostMapping("/register/utente")
    public Mono<ResponseEntity<Object>> registerUtente(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/register/utente", payload);
    }

    @PostMapping("/register/funcionario")
    public Mono<ResponseEntity<Object>> registerFuncionario(@RequestBody Map<String, Object> payload) {
        return proxyLoginOrRegister("/api/auth/register/funcionario", payload);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(ServerHttpRequest request) {
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
    public Mono<ResponseEntity<Object>> updatePassword(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Authenticated-User", required = false) String user,
            @RequestHeader(value = "X-Authenticated-Roles", required = false) String roles,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) String userId) {

        if (!StringUtils.hasText(user)) {
            return Mono.just(ResponseEntity.status(401).body((Object) null));
        }

        return webClient.put()
                .uri(marcacoesBaseUrl + "/api/auth/password")
                .header("X-Authenticated-User", user)
                .header("X-Authenticated-Roles", roles == null ? "" : roles)
                .header("X-Authenticated-User-Id", userId == null ? "" : userId)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .map(entity -> ResponseEntity.status(entity.getStatusCode()).body((Object) null))
                .onErrorResume(WebClientResponseException.class,
                    ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).body((Object) ex.getResponseBodyAsString())));
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

    private Mono<ResponseEntity<Object>> proxyLoginOrRegister(String path, Map<String, Object> payload) {
        return webClient.post()
            .uri(marcacoesBaseUrl + path)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(AuthResponse.class)
            .flatMap(downstream -> {
                if (downstream == null) {
                    return Mono.just(ResponseEntity.status(502)
                    .body((Object) Map.of("message", "Resposta inválida do serviço de autenticação")));
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

                return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body((Object) response));
            })
            .onErrorResume(WebClientResponseException.class,
                ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).body((Object) ex.getResponseBodyAsString())));
    }

    private String extractToken(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst("jwt");
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue();
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
