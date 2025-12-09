package pt.florinhas.marcacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
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
}

