package pt.florinhas.marcacoes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UpdatePasswordRequest;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.AuthService.AuthResult;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.environment:production}")
    private String environment;

    @Value("${app.secure-cookies:true}")
    private boolean secureCookies;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(
            @RequestBody LoginFuncionarioRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResult result = authService.loginFuncionario(request);
        return buildResponseWithCookie(result, httpRequest, httpResponse);
    }

    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(
            @RequestBody LoginUtenteRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResult result = authService.loginUtente(request);
        return buildResponseWithCookie(result, httpRequest, httpResponse);
    }

    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> registerUtente(
            @RequestBody UtenteRegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResult result = authService.registerUtente(request);
        return buildResponseWithCookie(result, httpRequest, httpResponse);
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> registerFuncionario(
            @RequestBody FuncionarioRegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthResult result = authService.registerFuncionario(request);
        return buildResponseWithCookie(result, httpRequest, httpResponse);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        authService.updatePassword(utilizador.getId(), request.newPassword(), request.termsAccepted());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal Utilizador utilizador,
            HttpServletRequest request, HttpServletResponse httpResponse) {
        if (utilizador == null) {
            return ResponseEntity.status(401).build();
        }

        // Force CSRF token to be loaded/generated to ensure cookie is present
        // This fixes issues where browser restart keeps JWT but loses Session-only CSRF
        // cookie
        var csrfToken = (org.springframework.security.web.csrf.CsrfToken) request
                .getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        if (csrfToken != null) {
            // Calling getToken() triggers the StatelessCsrfTokenRepository to write the
            // cookie
            csrfToken.getToken();
            log.debug("CSRF token refreshed for user: {}", utilizador.getEmail());
        }

        String role = utilizador.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UTENTE");

        // Return user info WITHOUT regenerating JWT cookie (unless we wanted to refresh
        // it too)
        AuthResponse response = new AuthResponse(
                utilizador.getId(),
                utilizador.getEmail(),
                utilizador.getNome(),
                role,
                utilizador.getNif(),
                utilizador.getTelefone(),
                System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24h from now
                true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Clear JWT cookie - must match exact parameters from login
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Delete immediately
                .sameSite("Strict")
                .build();

        // Clear CSRF cookie - must match exact parameters from SecurityConfig
        ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false) // MUST match login (false for CSRF)
                .secure(false)
                .path("/")
                .maxAge(0) // Delete immediately
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString())
                .build();
    }

    private ResponseEntity<AuthResponse> buildResponseWithCookie(
            AuthResult result,
            HttpServletRequest request,
            HttpServletResponse response) {
        // Determine secure flag: true in production, false in dev
        boolean isSecure = "development".equalsIgnoreCase(environment) ? false : secureCookies;

        // Create JWT cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", result.token())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(-1) // Session cookie (Logout on browser close)
                .sameSite("Lax")
                .build();

        log.debug("JWT cookie: secure={}, env={}", isSecure, environment);

        // The CsrfCookieFilter will automatically add the XSRF-TOKEN cookie to the
        // response
        // because we're accessing the CsrfToken attribute
        var csrfToken = (org.springframework.security.web.csrf.CsrfToken) request
                .getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        if (csrfToken != null) {
            // Force token to be loaded (triggers cookie creation)
            csrfToken.getToken();
            log.debug("CSRF token loaded: {}", csrfToken.getHeaderName());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }
}
