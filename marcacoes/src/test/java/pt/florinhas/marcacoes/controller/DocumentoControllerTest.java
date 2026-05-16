package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.DocumentoService;
import pt.florinhas.marcacoes.service.AuditLogService;

class DocumentoControllerTest {

    @Mock private DocumentoService documentoService;
    @Mock private AuthorizationService authService;
    @Mock private MarcacaoRepository marcacaoRepository;
    @Mock private AuditLogService auditLogService;

    private DocumentoController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new DocumentoController(documentoService, authService, marcacaoRepository, auditLogService);
    }

    private DocumentoDTO buildDocumentoDto(Long id, Long marcacaoId) {
        return new DocumentoDTO(id, "doc.pdf", "application/pdf", 123L, LocalDateTime.now(), marcacaoId, "Utente", "100000002", 1, "Finalidade");
    }

    private Marcacao buildMarcacaoWithOwner(Long userId) {
        Marcacao m = new Marcacao();
        pt.florinhas.common_data.domain.Utente u = new pt.florinhas.common_data.domain.Utente();
        u.setId(userId);
        m.setCriadoPor(u);
        return m;
    }

    @Test
    @DisplayName("Deve permitir upload de documento quando é admin")
    void uploadDocumento_DeveDelegarQuandoAdmin() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());
        DocumentoDTO dto = buildDocumentoDto(1L, 1L);

        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.uploadDocumento(eq(1L), eq(file), anyString())).thenReturn(dto);

        ResponseEntity<DocumentoDTO> result = controller.uploadDocumento(1L, file, "finalidade");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Deve falhar upload quando não é dono nem admin")
    void uploadDocumento_DeveFalharQuandoNaoEhDonoNemAdmin() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());

        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(buildMarcacaoWithOwner(99L)));

        assertThrows(AccessDeniedException.class, () -> controller.uploadDocumento(1L, file, "finalidade"));
    }

    @Test
    @DisplayName("Deve listar documentos quando tem permissão")
    void listarDocumentos_DeveDelegarQuandoDono() {
        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.listarDocumentosDaMarcacao(1L)).thenReturn(List.of(buildDocumentoDto(1L, 1L)));

        ResponseEntity<List<DocumentoDTO>> result = controller.listarDocumentos(1L);

        assertEquals(200, result.getStatusCode().value());
        assertFalse(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("Deve remover documento e retornar No Content")
    void removerDocumento_DeveRetornarNoContent() {
        DocumentoDTO dto = buildDocumentoDto(2L, 1L);
        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.obterDocumento(2L)).thenReturn(dto);

        ResponseEntity<Void> result = controller.removerDocumento(2L);

        assertEquals(204, result.getStatusCode().value());
        verify(documentoService).removerDocumento(2L);
    }
}