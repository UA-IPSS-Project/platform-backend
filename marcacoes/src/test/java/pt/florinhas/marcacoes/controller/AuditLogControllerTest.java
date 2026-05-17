package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.domain.AuditLog;
import pt.florinhas.marcacoes.dto.InternalAuditRequest;
import pt.florinhas.marcacoes.repository.AuditLogRepository;
import pt.florinhas.marcacoes.service.AuditLogService;

class AuditLogControllerTest {

    @Test
    @DisplayName("AuditLogController deve ser criado")
    void deveCriarController() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe AuditLogController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(AuditLogController.class);
    }

    @Test
    @DisplayName("getLogs deve retornar BadRequest quando a página for menor que 0")
    void getLogs_DeveRetornarBadRequest_QuandoPaginaInvalida() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");

        ResponseEntity<Object> response = controller.getLogs(null, null, null, null, null, -1, 50);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid 'page' parameter: must be greater than or equal to 0.", response.getBody());
    }

    @Test
    @DisplayName("getLogs deve retornar BadRequest quando o tamanho for menor que 1 ou maior que 200")
    void getLogs_DeveRetornarBadRequest_QuandoTamanhoInvalido() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");

        ResponseEntity<Object> responseLow = controller.getLogs(null, null, null, null, null, 0, 0);
        assertEquals(400, responseLow.getStatusCode().value());
        assertEquals("Invalid 'size' parameter: must be between 1 and 200.", responseLow.getBody());

        ResponseEntity<Object> responseHigh = controller.getLogs(null, null, null, null, null, 0, 201);
        assertEquals(400, responseHigh.getStatusCode().value());
        assertEquals("Invalid 'size' parameter: must be between 1 and 200.", responseHigh.getBody());
    }

    @Test
    @DisplayName("getLogs deve retornar OK com página de logs quando parâmetros forem válidos")
    void getLogs_DeveRetornarOkComLogs_QuandoParametrosValidos() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");

        Page<AuditLog> pageMock = Page.empty();
        when(service.findWithFilters(any(), any(), any(), any(), any(), any())).thenReturn(pageMock);

        ResponseEntity<Object> response = controller.getLogs(1L, "LOGIN", "UTILIZADOR", null, null, 0, 50);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(service).findWithFilters(eq(1L), eq("LOGIN"), eq("UTILIZADOR"), any(), any(), any());
    }

    @Test
    @DisplayName("logInternal deve retornar Forbidden (403) quando a chave secreta for incorreta")
    void logInternal_DeveRetornarForbidden_QuandoSecretIncorreto() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");

        InternalAuditRequest request = new InternalAuditRequest(1L, "admin", "LOGIN", "USER", 1L, "Login do admin",
                "127.0.0.1");
        ResponseEntity<Void> response = controller.logInternal("wrong-secret", request);

        assertEquals(403, response.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("logInternal deve salvar o log e retornar OK (200) quando a chave secreta for correta")
    void logInternal_DeveSalvarLogERetornarOk_QuandoSecretCorreto() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");

        InternalAuditRequest request = new InternalAuditRequest(1L, "admin", "LOGIN", "USER", 1L, "Login do admin",
                "127.0.0.1");
        ResponseEntity<Void> response = controller.logInternal("secret", request);

        assertEquals(200, response.getStatusCode().value());
        verify(repository).save(any(AuditLog.class));
    }
}