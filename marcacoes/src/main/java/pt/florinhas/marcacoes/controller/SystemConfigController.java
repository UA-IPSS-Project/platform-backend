package pt.florinhas.marcacoes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pt.florinhas.marcacoes.service.SystemConfigService;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasRole('DPO')")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/documento/retencao")
    public ResponseEntity<Map<String, Object>> getRetencaoDocumentos() {
        int anos = systemConfigService.getConfigValueAsInt("documento.retencao.anos", 5);
        return ResponseEntity.ok(Map.of(
            "anos", anos,
            "descricao", "Prazo de retenção de documentos em anos"
        ));
    }

    @PutMapping("/documento/retencao")
    public ResponseEntity<Map<String, Object>> setRetencaoDocumentos(@RequestBody Map<String, Integer> request) {
        Integer anos = request.get("anos");
        if (anos == null || anos < 1 || anos > 50) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Prazo deve estar entre 1 e 50 anos"));
        }

        systemConfigService.setConfigValue(
            "documento.retencao.anos",
            String.valueOf(anos),
            "Prazo de retenção de documentos em anos (RGPD)"
        );

        return ResponseEntity.ok(Map.of(
            "anos", anos,
            "mensagem", "Prazo de retenção atualizado com sucesso"
        ));
    }
}
