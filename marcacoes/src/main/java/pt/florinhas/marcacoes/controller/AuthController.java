package pt.florinhas.marcacoes.controller;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UserResponse;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.service.AuthService;

/**
 * Controller responsável pela autenticação e registo de utilizadores.
 *
 * Expõe endpoints REST para:
 * - login de utentes
 * - login de funcionários
 * - registo de novos utilizadores
 * - obtenção da informação do utilizador autenticado
 */
@RestController
@RequestMapping("/api/auth")

public class AuthController {

    /**
     * Serviço de autenticação que encapsula toda a lógica de negócio
     * relacionada com login, registo e geração de tokens JWT.
     */
    private final AuthService authService;

    /**
     * Construtor com injeção do serviço de autenticação.
     *
     * param authService serviço responsável pela lógica de autenticação
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Helper para criar o cookie HTTP Only com o JWT.
     */
    private org.springframework.http.ResponseCookie createJwtCookie(String token) {
        return org.springframework.http.ResponseCookie.from("jwt_auth", token)
                .httpOnly(true)
                .secure(false) // Em produção (HTTPS) deve ser true
                .path("/")
                .maxAge(-1) // Cookie de Sessão (apagado ao fechar browser)
                .sameSite("Lax") // 'Strict' pode causar problemas em dev com portas diferentes
                .build();
    }

    /**
     * Endpoint de login para funcionários.
     */
    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(
            @Valid @RequestBody LoginFuncionarioRequest request,
            jakarta.servlet.http.HttpServletResponse response) {

        var authResult = authService.loginFuncionario(request);

        // Define o cookie na resposta
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE,
                createJwtCookie(authResult.token()).toString());

        return ResponseEntity.ok(authResult.response());
    }

    /**
     * Endpoint de login para utentes.
     */
    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(
            @Valid @RequestBody LoginUtenteRequest request,
            jakarta.servlet.http.HttpServletResponse response) {

        var authResult = authService.loginUtente(request);

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE,
                createJwtCookie(authResult.token()).toString());

        return ResponseEntity.ok(authResult.response());
    }

    /**
     * Endpoint de registo de um novo utente.
     */
    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UtenteRegisterRequest request,
            jakarta.servlet.http.HttpServletResponse response) {

        var authResult = authService.registerUtente(request);

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE,
                createJwtCookie(authResult.token()).toString());

        return ResponseEntity.ok(authResult.response());
    }

    /**
     * Endpoint de registo de um novo funcionário.
     */
    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody FuncionarioRegisterRequest request,
            jakarta.servlet.http.HttpServletResponse response) {

        var authResult = authService.registerFuncionario(request);

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE,
                createJwtCookie(authResult.token()).toString());

        return ResponseEntity.ok(authResult.response());
    }

    /**
     * Endpoint de Logout.
     * Limpa o cookie de autenticação.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt_auth", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Expira imediatamente
                .sameSite("Lax")
                .build();

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint que devolve a informação do utilizador atualmente autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            org.springframework.security.core.Authentication authentication) {

        // Mantém a lógica existente
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .build();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Utilizador u) {
            String role = u instanceof Funcionario ? "FUNCIONARIO" : "UTENTE";
            UserResponse resp = new UserResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getNome(),
                    role,
                    u.getNif(),
                    u.getTelefone());

            return ResponseEntity.ok(resp);
        }
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .build();
    }

    @PostMapping("/set-password")
    public ResponseEntity<Void> setPassword(
            @Valid @RequestBody pt.florinhas.marcacoes.dto.SetPasswordRequest request,
            org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Utilizador u) {
            authService.updatePassword(u.getId(), request.password(), request.termsAccepted());
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
    }
}
