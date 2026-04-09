package pt.florinhas.marcacoes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.dto.AssuntoRequest;
import pt.florinhas.marcacoes.dto.AtualizarEstadoAssuntoRequest;
import pt.florinhas.marcacoes.service.AssuntoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assuntos")
@RequiredArgsConstructor
public class AssuntoController {

    private final AssuntoService assuntoService;

    @GetMapping
    public ResponseEntity<List<Assunto>> listarAtivos() {
        return ResponseEntity.ok(assuntoService.listarAtivos());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<List<Assunto>> listarTodos() {
        return ResponseEntity.ok(assuntoService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Assunto> criar(@RequestBody Assunto assunto) {
        return ResponseEntity.status(201).body(assuntoService.criar(assunto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Assunto> atualizar(@PathVariable Long id, @RequestBody Assunto assunto) {
        return ResponseEntity.ok(assuntoService.atualizar(id, assunto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<?> apagar(@PathVariable Long id) {
        assuntoService.apagar(id);
        return ResponseEntity.ok(Map.of("message", "Assunto desativado com sucesso"));
    }

    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Assunto> atualizarEstado(
            @PathVariable Long id, 
            @Valid @RequestBody AtualizarEstadoAssuntoRequest request) {
        return ResponseEntity.ok(assuntoService.setAtivo(id, request.ativo()));
    }
}
