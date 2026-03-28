package pt.florinhas.marcacoes.controller;

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
        emailService.sendGenericEmail(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok().build();
    }
}
