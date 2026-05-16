package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.repository.DocumentoRepository;

class DocumentoRetentionServiceTest {

    private DocumentoRepository documentoRepository;
    private DocumentoService documentoService;
    private AuditLogService auditLogService;

    private DocumentoRetentionService service;

    @BeforeEach
    void setup() {

        documentoRepository =
                mock(DocumentoRepository.class);

        documentoService =
                mock(DocumentoService.class);

        auditLogService =
                mock(AuditLogService.class);

        service =
                new DocumentoRetentionService(
                        documentoRepository,
                        documentoService,
                        auditLogService
                );
    }

    @Test
    void limparDocumentosExpirados_DeveIgnorarQuandoNaoExistemDocumentos() {

        when(documentoRepository
                .findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                service.limparDocumentosExpirados()
        );

        verify(documentoService, never())
                .removerDocumento(anyLong());

        verify(auditLogService, never())
                .log(anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void limparDocumentosExpirados_DeveRemoverDocumentosExpirados() {

        Documento documento =
                new Documento();

        documento.setId(1L);
        documento.setNomeOriginal("teste.pdf");
        documento.setDataExpiracao(
                LocalDateTime.now().minusDays(1)
        );

        when(documentoRepository
                .findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any(LocalDateTime.class)))
                .thenReturn(List.of(documento));

        assertDoesNotThrow(() ->
                service.limparDocumentosExpirados()
        );

        verify(documentoService)
                .removerDocumento(1L);

        verify(auditLogService)
                .log(
                        eq("LIMPEZA_AUTOMATICA"),
                        eq("DOCUMENTO"),
                        eq(1L),
                        contains("teste.pdf")
                );
    }

    @Test
    void limparDocumentosExpirados_DeveContinuarQuandoDocumentoFalha() {

        Documento documento1 =
                new Documento();

        documento1.setId(1L);
        documento1.setNomeOriginal("erro.pdf");
        documento1.setDataExpiracao(
                LocalDateTime.now().minusDays(1)
        );

        Documento documento2 =
                new Documento();

        documento2.setId(2L);
        documento2.setNomeOriginal("ok.pdf");
        documento2.setDataExpiracao(
                LocalDateTime.now().minusDays(1)
        );

        when(documentoRepository
                .findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any(LocalDateTime.class)))
                .thenReturn(List.of(documento1, documento2));

        doThrow(new RuntimeException("Erro teste"))
                .when(documentoService)
                .removerDocumento(1L);

        assertDoesNotThrow(() ->
                service.limparDocumentosExpirados()
        );

        verify(documentoService)
                .removerDocumento(1L);

        verify(documentoService)
                .removerDocumento(2L);

        verify(auditLogService, times(1))
                .log(
                        eq("LIMPEZA_AUTOMATICA"),
                        eq("DOCUMENTO"),
                        eq(2L),
                        contains("ok.pdf")
                );
    }

    @Test
    void limparDocumentosExpirados_DeveCapturarErroGlobal() {

        when(documentoRepository
                .findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Erro global"));

        assertDoesNotThrow(() ->
                service.limparDocumentosExpirados()
        );

        verify(documentoService, never())
                .removerDocumento(anyLong());

        verify(auditLogService, never())
                .log(anyString(), anyString(), anyLong(), anyString());
    }
}