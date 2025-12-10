package pt.florinhas.marcacoes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;

@RestController
@RequestMapping("/api/utilizadores")
@CrossOrigin(origins = "*")
public class UtilizadorController {

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    // Buscar utilizador por NIF
    @GetMapping("/nif/{nif}")
    public ResponseEntity<Utilizador> buscarPorNif(@PathVariable String nif) {
        try {
            Utilizador utilizador = utilizadorRepository.findByNif(nif).orElseThrow(() -> new RuntimeException("Utilizador não encontrado com NIF: " + nif));
            return ResponseEntity.ok(utilizador);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
