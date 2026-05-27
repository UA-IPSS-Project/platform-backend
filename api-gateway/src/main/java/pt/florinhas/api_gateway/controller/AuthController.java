package pt.florinhas.api_gateway.controller;

import java.util.List;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import pt.florinhas.api_gateway.dto.*;
import pt.florinhas.api_gateway.security.JwtService;
import pt.florinhas.api_gateway.security.JwtService.AuthUserClaims;
import pt.florinhas.api_gateway.service.AuthService;
import pt.florinhas.api_gateway.service.AuthService.AuthResult;

import pt.florinhas.common_data.domain.Utilizador;

import pt.florinhas.api_gateway.service.AuditClient;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuditClient auditClient;

    @Value("${app.secure-cookies:false}")
    private boolean secureCookies;

    @Value("${app.cookie-samesite:Lax}")
    private String cookieSameSite;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public AuthController(AuthService authService, JwtService jwtService, AuditClient auditClient) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.auditClient = auditClient;
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(
            @RequestBody LoginFuncionarioRequest request,
            ServerHttpRequest httpRequest) {
        AuthResult result = authService.loginFuncionario(request);
        auditClient.logAsync(result.response().id(), result.response().nome(),
                "LOGIN_FUNCIONARIO", "UTILIZADOR", result.response().id(),
                "Login de funcionario (" + result.response().role() + "): " + result.response().email(),
                getClientIp(httpRequest));
        return withJwtCookie(result.response(), httpRequest);
    }

    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(
            @RequestBody LoginUtenteRequest request,
            ServerHttpRequest httpRequest) {
        AuthResult result = authService.loginUtente(request);
        auditClient.logAsync(result.response().id(), result.response().nome(),
                "LOGIN_UTENTE", "UTILIZADOR", result.response().id(),
                "Login de utente: " + result.response().email(),
                getClientIp(httpRequest));
        return withJwtCookie(result.response(), httpRequest);
    }

    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> registerUtente(
            @Valid @RequestBody UtenteRegisterRequest request,
            ServerHttpRequest httpRequest) {
        AuthResult result = authService.registerUtente(request);
        auditClient.logAsync(result.response().id(), result.response().nome(),
                "REGISTO_UTENTE", "UTILIZADOR", result.response().id(),
                "Novo registo de utente: " + result.response().email(),
                getClientIp(httpRequest));
        return withJwtCookie(result.response(), httpRequest);
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> registerFuncionario(
            @Valid @RequestBody FuncionarioRegisterRequest request,
            ServerHttpRequest httpRequest) {
        AuthResult result = authService.registerFuncionario(request);
        auditClient.logAsync(result.response().id(), result.response().nome(),
                "REGISTO_FUNCIONARIO", "UTILIZADOR", result.response().id(),
                "Novo registo de funcionario " + result.response().role() + ": " + result.response().email(),
                getClientIp(httpRequest));
        return withJwtCookie(result.response(), httpRequest);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        authService.updatePassword(utilizador.getId(), request.newPassword(), request.termsAccepted());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recover-password")
    public ResponseEntity<Void> recoverPassword(@Valid @RequestBody RecoverPasswordRequest request) {
        authService.recoverPassword(request.identifier());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal Utilizador utilizador) {
        var response = authService.getCurrentUserResponse(utilizador);
        if (response.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(response.get());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Utilizador utilizador,
            ServerHttpRequest httpRequest) {
        if (utilizador != null) {
            auditClient.logAsync(utilizador.getId(), utilizador.getNome(),
                    "LOGOUT", "UTILIZADOR", utilizador.getId(),
                    "Logout: " + utilizador.getEmail(),
                    getClientIp(httpRequest));
        }
        boolean cookieSecure = shouldUseSecureCookie(httpRequest);
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(0)
            .sameSite(cookieSameSite)
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .build();
    }

    private String getClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp;
        var addr = request.getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    private ResponseEntity<AuthResponse> withJwtCookie(AuthResponse response, ServerHttpRequest httpRequest) {
        boolean cookieSecure = shouldUseSecureCookie(httpRequest);
        String roleName = response.role() == null ? "UTENTE" : response.role();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        String token = jwtService.generateToken(new AuthUserClaims(
            response.id(),
            response.email(),
            response.nome(),
            response.role(),
            response.nif(),
            response.telefone(),
            List.of(new SimpleGrantedAuthority(roleName))));

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(Duration.ofMillis(jwtExpiration))
            .sameSite(cookieSameSite)
            .build();

        log.debug("[LOGIN] Cookie jwt criado: secure={} sameSite={} path=/ para user={}",
                cookieSecure, cookieSameSite, response.email());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(response);
    }

    private boolean shouldUseSecureCookie(ServerHttpRequest request) {
        String scheme = request.getURI().getScheme();
        String forwardedProto = request.getHeaders().getFirst("X-Forwarded-Proto");
        boolean isHttps = "https".equalsIgnoreCase(scheme)
                || (forwardedProto != null && forwardedProto.toLowerCase().contains("https"));

        log.debug("[COOKIE] secureCookies={} scheme={} X-Forwarded-Proto={} -> secure={}",
                secureCookies, scheme, forwardedProto, secureCookies && isHttps);

        if (!secureCookies) return false;
        return isHttps;
    }
}
