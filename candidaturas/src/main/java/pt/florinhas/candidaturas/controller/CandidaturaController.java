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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CandidaturaController {
    private final CandidaturaService candidaturaService;
    private final FormService formService;

    // Forms
    @GetMapping("/forms")
    public ResponseEntity<List<Form>> GetForms() {
        List<Form> forms = formService.getForms();
        return ResponseEntity.ok(forms);
    }

    @PostMapping("/forms")
    public ResponseEntity<Form> createForm(@RequestBody Form form) {
        Form createdForm = formService.createForm(form);
        if (createdForm != null) {
            return ResponseEntity.ok(createdForm);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/forms/{id}")
    public ResponseEntity<Form> updateForm(@PathVariable String id, @RequestBody Form form) {
        Form updatedForm = formService.updateForm(id, form);
        if (updatedForm != null) {
            return ResponseEntity.ok(updatedForm);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/forms/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable String id) {
        if (!formService.deleteForm(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/forms/{id}")
    public ResponseEntity<Form> getFormByID(@PathVariable String id) {
        Form form = formService.getFormById(id);
        if (form != null) {
            return ResponseEntity.ok(form);
        }
        return ResponseEntity.notFound().build();
    }

    // Candidaturas
    @GetMapping("/candidaturas")

    public ResponseEntity<List<Candidatura>> getCandidaturas(
            @RequestParam(required = false) String nif,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) CandidaturaEstado estado,
            @RequestParam(required = false) Boolean assinado,
            @RequestParam(required = false) Integer idade) {
        List<Candidatura> candidaturas = candidaturaService.getCandidaturas(
                nif,
                nome,
                estado,
                assinado,
                idade);
        return ResponseEntity.ok(candidaturas);
    }

    @PostMapping("/candidaturas")
    public ResponseEntity<Candidatura> createCandidatura(@Valid @RequestBody Candidatura candidatura) {
        Candidatura createdCandidatura = candidaturaService.createCandidatura(candidatura);
        if (createdCandidatura != null) {
            return ResponseEntity.ok(createdCandidatura);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/candidaturas/{id}")
    public ResponseEntity<Candidatura> putCandidatura(@PathVariable String id,
            @Valid @RequestBody Candidatura candidatura) {
        Candidatura updatedCandidatura = candidaturaService.updateCandidatura(id, candidatura);
        if (updatedCandidatura != null) {
            return ResponseEntity.ok(updatedCandidatura);
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/candidaturas/{id}")
    public ResponseEntity<Void> deleteCandidatura(@PathVariable String id) {
        if (candidaturaService.deleteCandidatura(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidaturas/{id}")
    public ResponseEntity<Candidatura> getCandidaturaByID(@PathVariable String id) {
        Candidatura candidatura = candidaturaService.getCandidaturaById(id);
        if (candidatura != null) {
            return ResponseEntity.ok(candidatura);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidaturas/form/{formId}")
    public ResponseEntity<List<Candidatura>> getCandidaturasByFormID(@PathVariable String formId) {
        List<Candidatura> candidaturas = candidaturaService.getCandidaturasByFormId(formId);
        if (candidaturas != null && !candidaturas.isEmpty()) {
            return ResponseEntity.ok(candidaturas);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidaturas/user/{userId}")
    public ResponseEntity<List<Candidatura>> getCandidaturasByUserID(@PathVariable Long userId) {
        List<Candidatura> candidaturas = candidaturaService.getCandidaturasByUserId(userId);
        if (candidaturas != null && !candidaturas.isEmpty()) {
            return ResponseEntity.ok(candidaturas);
        }
        return ResponseEntity.notFound().build();

    }

}
