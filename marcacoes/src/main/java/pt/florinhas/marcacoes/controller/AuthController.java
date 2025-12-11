package pt.florinhas.marcacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(@Valid @RequestBody LoginFuncionarioRequest request) {
        return ResponseEntity.ok(authService.loginFuncionario(request));
    }
    
    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(@Valid @RequestBody LoginUtenteRequest request) {
        return ResponseEntity.ok(authService.loginUtente(request));
    }

    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UtenteRegisterRequest request) {
        return ResponseEntity.ok(authService.registerUtente(request));
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody FuncionarioRegisterRequest request) {
        return ResponseEntity.ok(authService.registerFuncionario(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Utilizador u) {
            String role = u instanceof Funcionario ? "FUNCIONARIO" : "UTENTE";
            UserResponse resp = new UserResponse(u.getId(), u.getEmail(), u.getNome(), role, u.getNif(), u.getTelefone());
            return ResponseEntity.ok(resp);
        }

        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
    }
}

