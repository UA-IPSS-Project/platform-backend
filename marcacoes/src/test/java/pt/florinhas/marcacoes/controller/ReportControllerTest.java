package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.domain.RelatorioPeriodico;
import pt.florinhas.marcacoes.dto.SendReportRequest;
import pt.florinhas.marcacoes.repository.RelatorioPeriodicoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

class ReportControllerTest {

    private EmailService emailService;
    private RelatorioPeriodicoRepository repository;

    private ReportController controller;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        repository = mock(RelatorioPeriodicoRepository.class);

        controller = new ReportController(emailService, repository);
    }

    @Test
    void listarPeriodicos_DeveRetornarLista() {

        when(repository.findByActivoTrue())
                .thenReturn(List.of());

        ResponseEntity<List<RelatorioPeriodico>> result = controller.listarPeriodicos();

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void criarPeriodico_DeveAtivarConfig() {

        RelatorioPeriodico relatorio = new RelatorioPeriodico();

        when(repository.save(relatorio))
                .thenReturn(relatorio);

        ResponseEntity<RelatorioPeriodico> result = controller.criarPeriodico(relatorio);

        assertEquals(true, result.getBody().isActivo());
    }

    @Test
    void atualizarPeriodico_DeveAtualizarCampos() {

        RelatorioPeriodico existing = new RelatorioPeriodico();
        RelatorioPeriodico updated = new RelatorioPeriodico();

        updated.setDestinatarios("a@a.com");
        updated.setFrequencia("SEMANAL");
        updated.setSeccoes("secretaria");

        when(repository.findById(1L))
                .thenReturn(java.util.Optional.of(existing));

        when(repository.save(existing))
                .thenReturn(existing);

        ResponseEntity<RelatorioPeriodico> result =
                controller.atualizarPeriodico(1L, updated);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("a@a.com", existing.getDestinatarios());
    }

    @Test
    void apagarPeriodico_DeveRetornar204() {

        ResponseEntity<Void> result = controller.apagarPeriodico(1L);

        assertEquals(204, result.getStatusCode().value());

        verify(repository).deleteById(1L);
    }

    @Test
    void sendReportByEmail_DeveEnviarEmailNormal() {

        SendReportRequest request = new SendReportRequest();

        request.setTo("teste@teste.com");
        request.setSeccoes(List.of("secretaria"));

        ResponseEntity<Void> result = controller.sendReportByEmail(request);

        assertEquals(200, result.getStatusCode().value());

        verify(emailService).sendGenericEmail(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}