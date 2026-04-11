package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.DocumentoService;

class DocumentoControllerTest {

    @Mock
    private DocumentoService documentoService;

    @Mock
    private AuthService authService;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    private DocumentoController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new DocumentoController(documentoService, authService, marcacaoRepository);
        SecurityContextHolder.clearContext();
    }

    private Marcacao buildMarcacaoWithOwner(Long ownerId) {
        Utente utente = new Utente();
        utente.setId(ownerId);

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        sec.setUtente(utente);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setMarcacaoSecretaria(sec);
        return marcacao;
    }

    private DocumentoDTO buildDocumentoDto(Long id, Long marcacaoId) {
        return new DocumentoDTO(
                id,
                "doc.pdf",
                "application/pdf",
                123L,
                LocalDateTime.now(),
                marcacaoId,
                "Utente",
                "100000002",
                1
        );
    }

    @Test
    void uploadDocumento_DeveDelegarQuandoAdmin() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());
        DocumentoDTO dto = buildDocumentoDto(1L, 1L);

        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.uploadDocumento(1L, file)).thenReturn(dto);

        ResponseEntity<DocumentoDTO> result = controller.uploadDocumento(1L, file);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1L, result.getBody().id());
    }

    @Test
    void uploadDocumento_DeveFalharQuandoNaoEhDonoNemAdmin() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());

        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(buildMarcacaoWithOwner(99L)));

        assertThrows(AccessDeniedException.class,
                () -> controller.uploadDocumento(1L, file));
    }

    @Test
    void listarDocumentos_DeveDelegarQuandoDono() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(buildMarcacaoWithOwner(10L)));
        when(documentoService.listarDocumentosDaMarcacao(1L)).thenReturn(List.of(buildDocumentoDto(1L, 1L)));

        ResponseEntity<List<DocumentoDTO>> result = controller.listarDocumentos(1L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveFalharGlobalSemRoleSecretaria() {
        when(authService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.pesquisarDocumentosPorMetadados(
                        null, null, null, null, null, null, null, null));
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveDelegarGlobalQuandoStaffNoContexto() {
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "user", "pw",
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SECRETARIA")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(documentoService.pesquisarDocumentosPorMetadados(
                null, null, null, null, null, null, null, null))
                .thenReturn(List.of());

        ResponseEntity<List<DocumentoDTO>> result =
                controller.pesquisarDocumentosPorMetadados(null, null, null, null, null, null, null, null);

        assertEquals(200, result.getStatusCode().value());
        verify(documentoService).pesquisarDocumentosPorMetadados(
                null, null, null, null, null, null, null, null);
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveDelegarPorMarcacaoQuandoTemPermissao() {
        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.pesquisarDocumentosPorMetadados(
                1L, "a", "b", "application/pdf", "u", "n", null, null))
                .thenReturn(List.of());

        ResponseEntity<List<DocumentoDTO>> result =
                controller.pesquisarDocumentosPorMetadados(1L, "a", "b", "application/pdf", "u", "n", null, null);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void notificarDocumentoInvalido_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.notificarDocumentoInvalido(1L, 2L, "obs");

        assertEquals(200, result.getStatusCode().value());
        verify(documentoService).notificarDocumentoInvalido(1L, 2L, "obs");
    }

    @Test
    void downloadDocumento_DeveRetornarAttachment() {
        DocumentoDTO dto = buildDocumentoDto(2L, 1L);
        Resource resource = new ByteArrayResource("abc".getBytes());

        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.obterDocumento(2L)).thenReturn(dto);
        when(documentoService.carregarFicheiro(2L)).thenReturn(resource);

        ResponseEntity<Resource> result = controller.downloadDocumento(2L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("application/pdf", result.getHeaders().getContentType().toString());
        assertTrue(result.getHeaders().getFirst("Content-Disposition").contains("attachment"));
    }

    @Test
    void previewDocumento_DeveRetornarInline() {
        DocumentoDTO dto = buildDocumentoDto(2L, 1L);
        Resource resource = new ByteArrayResource("abc".getBytes());

        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.obterDocumento(2L)).thenReturn(dto);
        when(documentoService.carregarFicheiro(2L)).thenReturn(resource);

        ResponseEntity<Resource> result = controller.previewDocumento(2L);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getHeaders().getFirst("Content-Disposition").contains("inline"));
    }

    @Test
    void obterMetadadosDocumento_DeveRetornarDto() {
        DocumentoDTO dto = buildDocumentoDto(2L, 1L);
        DocumentoMetadataDTO metadata = new DocumentoMetadataDTO(
                2L, "doc.pdf", "doc-store.pdf", "/x/doc-store.pdf", "application/pdf",
                123L, LocalDateTime.now(), 1L, "etag", "now", Map.of(), 1
        );

        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.obterDocumento(2L)).thenReturn(dto);
        when(documentoService.obterMetadadosDocumento(2L)).thenReturn(metadata);

        ResponseEntity<DocumentoMetadataDTO> result = controller.obterMetadadosDocumento(2L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(2L, result.getBody().id());
    }

    @Test
    void removerDocumento_DeveRetornarNoContent() {
        DocumentoDTO dto = buildDocumentoDto(2L, 1L);

        when(authService.isAdmin()).thenReturn(true);
        when(documentoService.obterDocumento(2L)).thenReturn(dto);

        ResponseEntity<Void> result = controller.removerDocumento(2L);

        assertEquals(204, result.getStatusCode().value());
        verify(documentoService).removerDocumento(2L);
    }
}