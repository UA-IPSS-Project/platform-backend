package pt.florinhas.marcacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseCookie; // Added
import org.springframework.http.HttpHeaders; // Added

import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UpdatePasswordRequest;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.AuthService.AuthResult;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(@RequestBody LoginFuncionarioRequest request) {
        AuthResult result = authService.loginFuncionario(request);
        return buildResponseWithCookie(result);
    }

    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(@RequestBody LoginUtenteRequest request) {
        AuthResult result = authService.loginUtente(request);
        return buildResponseWithCookie(result);
    }

    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> registerUtente(@RequestBody UtenteRegisterRequest request) {
        AuthResult result = authService.registerUtente(request);
        return buildResponseWithCookie(result);
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> registerFuncionario(@RequestBody FuncionarioRegisterRequest request) {
        AuthResult result = authService.registerFuncionario(request);
        return buildResponseWithCookie(result);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        authService.updatePassword(utilizador.getId(), request.newPassword(), request.termsAccepted());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal Utilizador utilizador) {
        if (utilizador == null) {
            return ResponseEntity.status(401).build();
        }

        String role = utilizador.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UTENTE");

        // Return user info WITHOUT regenerating cookies
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

    private ResponseEntity<AuthResponse> buildResponseWithCookie(AuthResult result) {
        // Session cookie (expires when browser closes)
        ResponseCookie cookie = ResponseCookie.from("jwt", result.token())
                .httpOnly(true)
                .secure(false) // Em dev (localhost) pode ser false; em prod mudar para true
                .path("/")
                // NO maxAge → session cookie (deleted on browser close)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }
}
