package pt.florinhas.marcacoes.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.marcacoes.service.TermsService;

import java.util.Map;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    /** Versão atual dos termos. */
    @GetMapping("/version")
    public ResponseEntity<Map<String, Integer>> getVersion() {
        return ResponseEntity.ok(Map.of("version", termsService.getCurrentVersion()));
    }

    /** Conteúdo dos termos no idioma pedido (pt/en). */
    @GetMapping("/content")
    public ResponseEntity<Map<String, String>> getContent(@RequestParam(defaultValue = "pt") String lang) {
        return ResponseEntity.ok(Map.of("content", termsService.getTermsContent(lang)));
    }

    /** Verifica se o utilizador autenticado precisa de re-aceitar os termos. */
    @GetMapping("/needs-acceptance")
    public ResponseEntity<Map<String, Boolean>> needsAcceptance(@AuthenticationPrincipal Utilizador user) {
        return ResponseEntity.ok(Map.of("needsAcceptance", termsService.needsAcceptance(user)));
    }

    /** Regista a aceitação dos termos pelo utilizador autenticado. */
    @PostMapping("/accept")
    public ResponseEntity<Void> accept(@AuthenticationPrincipal Utilizador user) {
        termsService.acceptTerms(user);
        return ResponseEntity.ok().build();
    }

    /** Publica nova versão dos termos (DPO only). */
    @PostMapping("/publish")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<Map<String, Integer>> publish(
            @Valid @RequestBody PublishTermsRequest request,
            @AuthenticationPrincipal Utilizador dpo) {
        int newVersion = termsService.publishTerms(
                request.contentPt(), request.contentEn(), request.changeDescription(), dpo.getId());
        return ResponseEntity.ok(Map.of("version", newVersion));
    }

    record PublishTermsRequest(
            @NotBlank String contentPt,
            @NotBlank String contentEn,
            @NotBlank String changeDescription) {}
}