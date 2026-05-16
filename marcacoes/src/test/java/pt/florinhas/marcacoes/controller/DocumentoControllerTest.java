package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.AuditLogService;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.DocumentoService;

class DocumentoControllerTest {

    @Test
    @DisplayName("DocumentoController deve ser criado")
    void deveCriarController() {
        DocumentoService documentoService = mock(DocumentoService.class);
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        MarcacaoRepository marcacaoRepository = mock(MarcacaoRepository.class);
        AuditLogService auditLogService = mock(AuditLogService.class);

        DocumentoController controller = new DocumentoController(
                documentoService,
                authorizationService,
                marcacaoRepository,
                auditLogService
        );

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe DocumentoController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(DocumentoController.class);
    }
}