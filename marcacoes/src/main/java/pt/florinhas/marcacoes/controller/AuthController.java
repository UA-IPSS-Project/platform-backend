package pt.florinhas.marcacoes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UpdatePasswordRequest;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.AuthService.AuthResult;
import pt.florinhas.marcacoes.service.UtilizadorService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UtilizadorService utilizadorService;

    public AuthController(AuthService authService, UtilizadorService utilizadorService) {
        this.authService = authService;
        this.utilizadorService = utilizadorService;
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(
            @RequestBody LoginFuncionarioRequest request) {
        AuthResult result = authService.loginFuncionario(request);
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(
            @RequestBody LoginUtenteRequest request) {
        AuthResult result = authService.loginUtente(request);
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> registerUtente(
            @Valid @RequestBody UtenteRegisterRequest request) {
        AuthResult result = authService.registerUtente(request);
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> registerFuncionario(
            @Valid @RequestBody FuncionarioRegisterRequest request) {
        AuthResult result = authService.registerFuncionario(request);
        return ResponseEntity.ok(result.response());
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

        var persistedUser = utilizadorService.obterUtilizadorPorId(utilizador.getId());
        boolean active = true;
        boolean requiresPasswordSetup = false;
        if (persistedUser instanceof Utente u) {
            active = u.isActivo();
        } else if (persistedUser instanceof Funcionario f) {
            active = f.isActivo();
        }
        requiresPasswordSetup = authService.requiresPasswordSetup(persistedUser, active);

        // Return user info WITHOUT regenerating JWT cookie (unless we wanted to refresh it too)
        AuthResponse response = new AuthResponse(
                utilizador.getId(),
                utilizador.getEmail(),
                utilizador.getNome(),
                role,
                utilizador.getNif(),
                utilizador.getTelefone(),
                System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24h from now
                active,
                requiresPasswordSetup);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
