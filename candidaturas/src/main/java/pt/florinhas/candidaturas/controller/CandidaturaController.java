package pt.florinhas.candidaturas.controller;

// Lombok
import lombok.AllArgsConstructor;

// Java
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import pt.florinhas.candidaturas.service.*;
import pt.florinhas.candidaturas.domain.*;
import pt.florinhas.candidaturas.dto.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CandidaturaController {
    private final CandidaturaService candidaturaService;
    private final FormService formService;

    // Forms
    @GetMapping("/forms")
    public ResponseEntity<List<FormResponse>> getForms() {
        List<FormResponse> forms = formService.getForms().stream()
                .map(FormResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/forms/types")
    public ResponseEntity<List<FormTypeResponse>> getFormTypes() {
        List<FormTypeResponse> forms = formService.getForms().stream()
                .map(FormTypeResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(forms);
    }

    @PostMapping("/forms")
    public ResponseEntity<FormResponse> createForm(
            @Valid @RequestBody FormCreate dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Form createdForm = formService.createForm(dto, userId);
        if (createdForm != null) {
            return ResponseEntity.ok(FormResponse.fromEntity(createdForm));
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/forms/{id}")
    public ResponseEntity<FormResponse> updateForm(
            @PathVariable String id,
            @Valid @RequestBody FormUpdate dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Form updatedForm = formService.updateForm(id, dto, userId);
        if (updatedForm != null) {
            return ResponseEntity.ok(FormResponse.fromEntity(updatedForm));
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
    public ResponseEntity<FormResponse> getFormByID(@PathVariable String id) {
        Form form = formService.getFormById(id);
        if (form != null) {
            return ResponseEntity.ok(FormResponse.fromEntity(form));
        }
        return ResponseEntity.notFound().build();
    }

    // Candidaturas
    @GetMapping("/candidaturas")
    public ResponseEntity<List<CandidaturaResponse>> getCandidaturas(
            @RequestParam(required = false) String nif,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) CandidaturaEstado estado,
            @RequestParam(required = false) Boolean assinado,
            @RequestParam(required = false) Integer idade) {
        List<CandidaturaResponse> candidaturas = candidaturaService.getCandidaturas(
                nif, nome, estado, assinado, idade).stream()
                .map(CandidaturaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(candidaturas);
    }

    @PostMapping("/candidaturas")
    public ResponseEntity<CandidaturaResponse> createCandidatura(
            @Valid @RequestBody CandidaturaCreate dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Candidatura createdCandidatura = candidaturaService.createCandidatura(dto, userId);
        if (createdCandidatura != null) {
            return ResponseEntity.ok(CandidaturaResponse.fromEntity(createdCandidatura));
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/candidaturas/{id}")
    public ResponseEntity<CandidaturaResponse> updateCandidatura(
            @PathVariable String id,
            @Valid @RequestBody CandidaturaUpdate dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Candidatura updatedCandidatura = candidaturaService.updateCandidatura(id, dto, userId);
        if (updatedCandidatura != null) {
            return ResponseEntity.ok(CandidaturaResponse.fromEntity(updatedCandidatura));
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/candidaturas/{id}/status")
    public ResponseEntity<CandidaturaResponse> updateCandidaturaStatus(
            @PathVariable String id,
            @Valid @RequestBody CandidaturaStatusUpdate dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Candidatura updatedCandidatura = candidaturaService.updateCandidaturaStatus(id, dto, userId);
        if (updatedCandidatura != null) {
            return ResponseEntity.ok(CandidaturaResponse.fromEntity(updatedCandidatura));
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
    public ResponseEntity<CandidaturaResponse> getCandidaturaByID(@PathVariable String id) {
        Candidatura candidatura = candidaturaService.getCandidaturaById(id);
        if (candidatura != null) {
            return ResponseEntity.ok(CandidaturaResponse.fromEntity(candidatura));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidaturas/form/{formId}")
    public ResponseEntity<List<CandidaturaResponse>> getCandidaturasByFormID(@PathVariable String formId) {
        List<CandidaturaResponse> candidaturas = candidaturaService.getCandidaturasByFormId(formId).stream()
                .map(CandidaturaResponse::fromEntity)
                .collect(Collectors.toList());
        if (!candidaturas.isEmpty()) {
            return ResponseEntity.ok(candidaturas);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidaturas/user/{userId}")
    public ResponseEntity<List<CandidaturaResponse>> getCandidaturasByUserID(@PathVariable Long userId) {
        List<CandidaturaResponse> candidaturas = candidaturaService.getCandidaturasByUserId(userId).stream()
                .map(CandidaturaResponse::fromEntity)
                .collect(Collectors.toList());
        if (!candidaturas.isEmpty()) {
            return ResponseEntity.ok(candidaturas);
        }
        return ResponseEntity.notFound().build();
    }
}
