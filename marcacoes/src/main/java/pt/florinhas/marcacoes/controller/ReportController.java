package pt.florinhas.marcacoes.controller;

import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.dto.SendReportRequest;
import pt.florinhas.marcacoes.service.email.EmailService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final EmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<Void> sendReportByEmail(@RequestBody SendReportRequest request) {
        if (request.getPdfBase64() != null && !request.getPdfBase64().isEmpty()) {
            // Remove data:application/pdf;base64, prefix if present
            String base64Data = request.getPdfBase64();
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            byte[] pdfBytes = Base64.getDecoder().decode(base64Data);
            emailService.sendEmailWithAttachment(
                    request.getTo(),
                    request.getSubject(),
                    request.getBody(),
                    pdfBytes,
                    request.getFileName() != null ? request.getFileName() : "relatorio.pdf");
        } else {
            emailService.sendGenericEmail(request.getTo(), request.getSubject(), request.getBody());
        }
        return ResponseEntity.ok().build();
    }
}