package pt.florinhas.marcacoes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequestDTO;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequestDTO;
import pt.florinhas.marcacoes.dto.UpdatePasswordRequest;
import pt.florinhas.marcacoes.service.UtilizadorService;
import pt.florinhas.marcacoes.service.KeycloakAdminClient;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UtilizadorService utilizadorService;

    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    @PostMapping("/register/utente")
    public ResponseEntity<Utilizador> registerUtente(@Valid @RequestBody UtenteRegisterRequestDTO request) {
        Utilizador utilizador = utilizadorService.registrarUtente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(utilizador);
    }

    @PostMapping("/register/funcionario")
    public ResponseEntity<Utilizador> registerFuncionario(@Valid @RequestBody FuncionarioRegisterRequestDTO request) {
        Utilizador utilizador = utilizadorService.registrarFuncionario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(utilizador);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
        Utilizador user = utilizadorService.getUtilizadorAutenticado();
        
        // 1. Update in Keycloak
        keycloakAdminClient.atualizarPassword(user.getEmail(), request.getNewPassword());
        
        // 2. Update locally in DB
        utilizadorService.atualizarPasswordLocal(user.getId(), request.getNewPassword());
        
        return ResponseEntity.ok().build();
    }
}
