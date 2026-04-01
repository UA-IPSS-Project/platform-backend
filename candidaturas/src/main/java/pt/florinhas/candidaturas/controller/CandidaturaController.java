package pt.florinhas.candidaturas.controller;

// Lombok
import lombok.AllArgsConstructor;

// Java
import java.util.List;

// From this project
import pt.florinhas.candidaturas.service.*;
import pt.florinhas.candidaturas.domain.*;

// Spring
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidaturas")
@AllArgsConstructor
public class CandidaturaController {
    private final CandidaturaService candidaturaService;
    private final FormService formService;

    // Forms
    @GetMapping("")
    public ResponseEntity<List<Form>> GetForms() {
        // TODO
        return null;
    }

    @PostMapping("")
    public ResponseEntity<Form> createForm(@RequestBody Form form) {
        // TODO
        return null;
    }

    @PutMapping("path/{id}")
    public ResponseEntity<Form> putMethodName(@PathVariable String id, @RequestBody Form entity) {
        // TODO
        return null;
    }

    @DeleteMapping("path/{id}")
    public ResponseEntity<String> deleteMethodName(@PathVariable String id) {
        // TODO
        return null;
    }

    // Candidaturas
    @GetMapping("/candidaturas")
    public ResponseEntity<List<Candidatura>> getCandidaturas() {
        // TODO
        return null;
    }

    @PostMapping("/candidaturas")
    public ResponseEntity<Candidatura> createCandidatura(@RequestBody Candidatura candidatura
    ) {
        // TODO
        return null;
    }

    @PutMapping("/candidaturas/{id}")
    public ResponseEntity<Candidatura> putCandidatura(@PathVariable String id, @RequestBody Candidatura candidatura) {
        // TODO
        return null;
    }
}
