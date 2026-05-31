package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.AuditLogService;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.DocumentoService;

class DocumentoControllerTest {

    private DocumentoService service;
    private AuthorizationService auth;
    private MarcacaoRepository repository;
    private AuditLogService audit;

    private DocumentoController controller;

    @BeforeEach
    void setUp() {

        service = mock(DocumentoService.class);
        auth = mock(AuthorizationService.class);
        repository = mock(MarcacaoRepository.class);
        audit = mock(AuditLogService.class);

        controller =
                new DocumentoController(
                        service,
                        auth,
                        repository,
                        audit);
    }

    @Test
    void listarDocumentos_DeveRetornarLista() {

        when(auth.hasAnyRole(
                "ROLE_SECRETARIA",
                "ROLE_BALNEARIO",
                "ROLE_FUNCIONARIO"))
                .thenReturn(true);

        when(service.listarDocumentosDaMarcacao(1L))
                .thenReturn(List.of());

        ResponseEntity<List<DocumentoDTO>> result =
                controller.listarDocumentos(1L);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void pesquisarDocumentos_DeveRetornarPagina() {

        when(auth.hasAnyRole(
                "ROLE_SECRETARIA",
                "ROLE_FUNCIONARIO"))
                .thenReturn(true);

        when(service.pesquisarDocumentosPorMetadados(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        ResponseEntity<?> result =
                controller.pesquisarDocumentosPorMetadados(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20));

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void pesquisarDocumentos_DeveFalharSemPermissao() {

        when(auth.hasAnyRole(
                "ROLE_SECRETARIA",
                "ROLE_FUNCIONARIO"))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.pesquisarDocumentosPorMetadados(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20)));
    }

    @Test
    void obterMetadadosDocumento_DeveRetornarDto() {

        DocumentoDTO dto =
                mock(DocumentoDTO.class);

        DocumentoMetadataDTO metadata =
                mock(DocumentoMetadataDTO.class);

        when(dto.marcacaoId())
                .thenReturn(1L);

        when(auth.hasAnyRole(
                "ROLE_SECRETARIA",
                "ROLE_BALNEARIO",
                "ROLE_FUNCIONARIO"))
                .thenReturn(true);

        when(service.obterDocumento(1L))
                .thenReturn(dto);

        when(service.obterMetadadosDocumento(1L))
                .thenReturn(metadata);

        ResponseEntity<DocumentoMetadataDTO> result =
                controller.obterMetadadosDocumento(1L);

        assertEquals(metadata, result.getBody());
    }

    @Test
    void removerDocumento_DeveRetornar204() {

        DocumentoDTO dto =
                mock(DocumentoDTO.class);

        when(dto.marcacaoId())
                .thenReturn(1L);

        when(dto.nomeOriginal())
                .thenReturn("teste.pdf");

        when(auth.hasAnyRole(
                "ROLE_SECRETARIA",
                "ROLE_BALNEARIO",
                "ROLE_FUNCIONARIO"))
                .thenReturn(true);

        when(service.obterDocumento(1L))
                .thenReturn(dto);

        ResponseEntity<Void> result =
                controller.removerDocumento(1L);

        assertEquals(204, result.getStatusCode().value());
    }
}
