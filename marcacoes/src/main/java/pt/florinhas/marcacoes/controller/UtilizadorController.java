package pt.florinhas.marcacoes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.UtilizadorInfoDTO;
import pt.florinhas.marcacoes.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.service.UtilizadorService;

@RestController
@RequestMapping("/api/utilizadores")
@CrossOrigin(origins = "*")
public class UtilizadorController {

    @Autowired
    private UtilizadorService utilizadorService;

    // Buscar utilizador por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obterUtilizadorPorId(@PathVariable Long id) {
        try {
            Utilizador utilizador = utilizadorService.obterUtilizadorPorId(id);
            UtilizadorResponseDTO response = UtilizadorResponseDTO.fromUtilizador(utilizador);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Buscar utilizador por NIF
    @GetMapping("/nif/{nif}")
    public ResponseEntity<Utilizador> buscarPorNif(@PathVariable String nif) {
        try {
            Utilizador utilizador = utilizadorService.buscarPorNif(nif).orElseThrow(() -> new RuntimeException("Utilizador não encontrado com NIF: " + nif));
            return ResponseEntity.ok(utilizador);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Atualizar informações do utilizador
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUtilizador(
            @PathVariable Long id,
            @RequestBody UtilizadorInfoDTO request) {
        try {
            Utilizador utilizador = utilizadorService.atualizarUtilizador(id, request);
            UtilizadorResponseDTO response = UtilizadorResponseDTO.fromUtilizador(utilizador);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Obter número de utentes
    @GetMapping("/utentes/count")
    public ResponseEntity<Long> contarUtentes() {
        long count = utilizadorService.contarUtentesAtivos();
        return ResponseEntity.ok(count);
    }
}
