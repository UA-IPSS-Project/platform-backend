package pt.florinhas.candidaturas.controller;

// Lombok
import lombok.AllArgsConstructor;

// Java
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    private final DocumentoService documentoService;

    // Forms
    @GetMapping("/forms")
    public ResponseEntity<List<FormResponse>> getForms(
            @RequestParam(required = false) pt.florinhas.candidaturas.domain.FormStatus status) {
        List<FormResponse> forms = (status != null
                ? formService.getFormsByStatus(status)
                : formService.getForms())
                .stream()
                .map(FormResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/forms/types")
    public ResponseEntity<List<FormTypeResponse>> getFormTypes(
            @RequestParam(required = false) Long utenteId) {
        List<Form> forms;
        if (utenteId != null) {
            List<Candidatura> candidaturas = candidaturaService.getCandidaturas(
                    null, utenteId, null, null, null, null, null);
            Set<String> formIds = candidaturas.stream()
                    .map(Candidatura::getFormId)
                    .collect(Collectors.toSet());
            forms = formService.getForms().stream()
                    .filter(f -> formIds.contains(f.getId()))
                    .collect(Collectors.toList());
        } else {
            forms = formService.getForms();
        }
        List<FormTypeResponse> result = forms.stream()
                .map(FormTypeResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forms")
    public ResponseEntity<FormResponse> createForm(
            @Valid @RequestBody FormCreate dto,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
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
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Form updatedForm = formService.updateForm(id, dto, userId);
        if (updatedForm != null) {
            return ResponseEntity.ok(FormResponse.fromEntity(updatedForm));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/forms/{id}/draft")
    public ResponseEntity<FormDraftResponse> getFormDraft(@PathVariable String id) {
        FormDraft draft = formService.getDraftByFormId(id);
        if (draft != null) {
            return ResponseEntity.ok(FormDraftResponse.fromEntity(draft));
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/forms/{id}/draft")
    public ResponseEntity<FormDraftResponse> saveFormDraft(
            @PathVariable String id,
            @RequestBody FormDraftSave dto,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        FormDraft draft = formService.saveOrUpdateDraft(id, dto, userId);
        if (draft != null) {
            return ResponseEntity.ok(FormDraftResponse.fromEntity(draft));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/forms/{id}/draft")
    public ResponseEntity<Void> deleteFormDraft(@PathVariable String id) {
        if (!formService.deleteDraftByFormId(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
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
            @RequestParam(required = false) String formId,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) String nif,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) CandidaturaEstado estado,
            @RequestParam(required = false) Boolean assinado,
            @RequestParam(required = false) Integer idade) {
        List<CandidaturaResponse> candidaturas = candidaturaService.getCandidaturas(
                formId, utenteId, nif, nome, estado, assinado, idade).stream()
                .map(CandidaturaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(candidaturas);
    }

    @PostMapping("/candidaturas")
    public ResponseEntity<CandidaturaResponse> createCandidatura(
            @Valid @RequestBody CandidaturaCreate dto,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
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
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
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
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
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

    @GetMapping("/candidaturas/{id}/secretary-draft")
    public ResponseEntity<SecretaryDraftResponse> getSecretaryDraft(@PathVariable String id) {
        var draft = candidaturaService.getSecretaryDraft(id);
        if (draft != null) {
            return ResponseEntity.ok(SecretaryDraftResponse.fromEntity(draft));
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/candidaturas/{id}/secretary-draft")
    public ResponseEntity<SecretaryDraftResponse> saveSecretaryDraft(
            @PathVariable String id,
            @RequestBody SecretaryDraftSave dto,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var draft = candidaturaService.saveSecretaryDraft(id, dto, userId);
        return ResponseEntity.ok(SecretaryDraftResponse.fromEntity(draft));
    }

    @DeleteMapping("/candidaturas/{id}/secretary-draft")
    public ResponseEntity<Void> deleteSecretaryDraft(@PathVariable String id) {
        if (!candidaturaService.deleteSecretaryDraft(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/candidaturas/{id}/secretary-draft/publish")
    public ResponseEntity<CandidaturaResponse> publishSecretaryDraft(
            @PathVariable String id,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Candidatura candidatura = candidaturaService.publishSecretaryDraft(id, userId);
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

    // Documentos
    @PostMapping("/candidaturas/{id}/documentos")
    public ResponseEntity<CandidaturaDocumentoDTO> uploadDocumento(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Authenticated-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            CandidaturaDocumentoDTO dto = documentoService.uploadDocumento(id, file, userId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/candidaturas/{id}/documentos")
    public ResponseEntity<List<CandidaturaDocumentoDTO>> listarDocumentos(@PathVariable String id) {
        return ResponseEntity.ok(documentoService.listarDocumentos(id));
    }

    @GetMapping("/candidaturas/documentos/{docId}/download")
    public ResponseEntity<Resource> downloadDocumento(@PathVariable String docId) {
        Resource resource = documentoService.downloadDocumento(docId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        String tipo = documentoService.getTipo(docId);
        String nomeOriginal = documentoService.getNomeOriginal(docId);
        MediaType mediaType = tipo != null ? MediaType.parseMediaType(tipo) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeOriginal + "\"")
                .body(resource);
    }

    @DeleteMapping("/candidaturas/documentos/{docId}")
    public ResponseEntity<Void> removerDocumento(@PathVariable String docId) {
        if (!documentoService.removerDocumento(docId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
