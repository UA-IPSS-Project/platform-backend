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
     * Endpoint de login para funcionários.
     *
     * Recebe as credenciais do funcionário, valida-as e devolve
     * um token de autenticação (ex.: JWT) juntamente com dados relevantes.
     *
     * param request DTO com as credenciais do funcionário
     * return AuthResponse com token e informação do utilizador
     */
    @PostMapping("/login/funcionario")
    public ResponseEntity<AuthResponse> loginFuncionario(
            @Valid @RequestBody LoginFuncionarioRequest request) {

        return ResponseEntity.ok(authService.loginFuncionario(request));
    }

    /**
     * Endpoint de login para utentes.
     *
     * Recebe as credenciais do utente, valida-as e devolve
     * um token de autenticação.
     *
     * param request DTO com as credenciais do utente
     * return AuthResponse com token e informação do utilizador
     */
    @PostMapping("/login/utente")
    public ResponseEntity<AuthResponse> loginUtente(
            @Valid @RequestBody LoginUtenteRequest request) {

        return ResponseEntity.ok(authService.loginUtente(request));
    }

    /**
     * Endpoint de registo de um novo utente.
     * Cria um novo utilizador do tipo utente e devolve automaticamente um token de
     * autenticação após o registo.
     * param request DTO com os dados de registo do utente
     * return AuthResponse com token e informação do utilizador criado
     */
    @PostMapping("/register/utente")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UtenteRegisterRequest request) {

        return ResponseEntity.ok(authService.registerUtente(request));
    }

    /**
     * Endpoint de registo de um novo funcionário.
     * Cria um novo utilizador do tipo funcionário e devolve um token de
     * autenticação associado.
     * param request DTO com os dados de registo do funcionário
     * return AuthResponse com token e informação do utilizador criado
     */
    @PostMapping("/register/funcionario")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody FuncionarioRegisterRequest request) {

        return ResponseEntity.ok(authService.registerFuncionario(request));
    }

    /**
     * Endpoint que devolve a informação do utilizador atualmente autenticado.
     * Utiliza o objeto Authentication injetado pelo Spring Security para obter o
     * utilizador associado ao token JWT enviado no pedido.
     * param authentication contexto de autenticação atual
     * return UserResponse com os dados do utilizador autenticado, ou 401 caso não
     * exista autenticação válida
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            org.springframework.security.core.Authentication authentication) {

        // Caso não exista autenticação ou esta não seja válida
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .build();
        }
        // Obtém o principal associado à autenticação
        Object principal = authentication.getPrincipal();
        // Verifica se o principal corresponde a um utilizador da aplicação
        if (principal instanceof Utilizador u) {
            // Determina o role com base no tipo concreto do utilizador
            String role = u instanceof Funcionario ? "FUNCIONARIO" : "UTENTE";
            // Constrói a resposta com os dados públicos do utilizador
            UserResponse resp = new UserResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getNome(),
                    role,
                    u.getNif(),
                    u.getTelefone());

            return ResponseEntity.ok(resp);
        }
        // Caso o principal não seja reconhecido
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
