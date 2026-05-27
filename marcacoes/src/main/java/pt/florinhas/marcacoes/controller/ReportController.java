package pt.florinhas.marcacoes.controller;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.domain.RelatorioPeriodico;
import pt.florinhas.marcacoes.dto.SendReportRequest;
import pt.florinhas.marcacoes.repository.RelatorioPeriodicoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final EmailService emailService;
    private final RelatorioPeriodicoRepository relatorioPeriodicoRepository;

    private static final Map<String, String> SECTION_LABELS = Map.of(
            "secretaria", "Marcações da Secretaria",
            "balneario", "Marcações do Balneário",
            "material", "Requisições de Material",
            "transporte", "Requisições de Transporte",
            "manutencao", "Requisições de Manutenção"
    );

    @PostMapping("/email")
    public ResponseEntity<Void> sendReportByEmail(@RequestBody SendReportRequest request) {
        String subject = request.getSubject();
        String body = buildReportBody(request);

        String[] recipients = request.getTo().split(",");
        for (String recipient : recipients) {
            String email = recipient.trim();
            if (email.isEmpty()) continue;
            if (request.getPdfBase64() != null && !request.getPdfBase64().isEmpty()) {
                String base64Data = request.getPdfBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                byte[] pdfBytes = Base64.getDecoder().decode(base64Data);
                emailService.sendEmailWithAttachment(email, subject, body, pdfBytes,
                        request.getFileName() != null ? request.getFileName() : "relatorio.pdf");
            } else {
                emailService.sendGenericEmail(email, subject, body);
            }
        }
        return ResponseEntity.ok().build();
    }

    private String buildReportBody(SendReportRequest request) {
        if (request.getSeccoes() == null || request.getSeccoes().isEmpty()) {
            return request.getBody() != null ? request.getBody() : "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Segue em anexo o relatório institucional");
        if (request.getPeriodoInicio() != null && request.getPeriodoFim() != null) {
            sb.append(" referente ao período de ").append(request.getPeriodoInicio())
              .append(" até ").append(request.getPeriodoFim());
        }
        sb.append(".\n\nDados incluídos:\n");
        for (String seccao : request.getSeccoes()) {
            String label = SECTION_LABELS.getOrDefault(seccao, seccao);
            sb.append("  • ").append(label).append("\n");
        }
        sb.append("\nCom os melhores cumprimentos,\nFlorinhas do Vouga");
        return sb.toString();
    }

    @GetMapping("/periodicos")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<List<RelatorioPeriodico>> listarPeriodicos() {
        return ResponseEntity.ok(relatorioPeriodicoRepository.findByActivoTrue());
    }

    @PostMapping("/periodicos")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<RelatorioPeriodico> criarPeriodico(@RequestBody RelatorioPeriodico config) {
        config.setActivo(true);
        return ResponseEntity.ok(relatorioPeriodicoRepository.save(config));
    }

    @PutMapping("/periodicos/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<RelatorioPeriodico> atualizarPeriodico(@PathVariable Long id, @RequestBody RelatorioPeriodico config) {
        RelatorioPeriodico existing = relatorioPeriodicoRepository.findById(id).orElseThrow();
        existing.setDestinatarios(config.getDestinatarios());
        existing.setFrequencia(config.getFrequencia());
        existing.setDataInicio(config.getDataInicio());
        existing.setSeccoes(config.getSeccoes());
        return ResponseEntity.ok(relatorioPeriodicoRepository.save(existing));
    }

    @DeleteMapping("/periodicos/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarPeriodico(@PathVariable Long id) {
        relatorioPeriodicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
