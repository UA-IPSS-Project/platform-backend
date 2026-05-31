package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.repository.DocumentoRepository;

class DocumentoRetentionServiceTest {

    private DocumentoRepository repository;
    private DocumentoService documentoService;
    private AuditLogService auditLogService;

    private DocumentoRetentionService service;

    @BeforeEach
    void setUp() {

        repository = mock(DocumentoRepository.class);
        documentoService = mock(DocumentoService.class);
        auditLogService = mock(AuditLogService.class);

        service = new DocumentoRetentionService(repository, documentoService, auditLogService);
    }

    @Test
    void limparDocumentosExpirados_DeveRemoverDocumentos() {

        Documento documento = new Documento();

        documento.setId(1L);
        documento.setNomeOriginal("teste.pdf");
        documento.setDataExpiracao(LocalDateTime.now().minusDays(1));

        when(repository.findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any()))
                .thenReturn(List.of(documento));

        service.limparDocumentosExpirados();

        verify(documentoService).removerDocumento(1L);

        verify(auditLogService).log(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void limparDocumentosExpirados_DeveIgnorarListaVazia() {

        when(repository.findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any()))
                .thenReturn(List.of());

        service.limparDocumentosExpirados();

        verify(documentoService, never())
                .removerDocumento(any());
    }

    @Test
    void limparDocumentosExpirados_NaoDeveLancarErro() {

        when(repository.findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any()))
                .thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                service.limparDocumentosExpirados());
    }
}