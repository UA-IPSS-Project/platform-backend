package pt.florinhas.marcacoes.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.DocumentoService;

/**
 * Controller REST para gestão de documentos anexados a marcações.
 * 
 * Endpoints:
 * - POST /api/documentos/marcacao/{marcacaoId}/upload - Upload de documento
 * - GET /api/documentos/marcacao/{marcacaoId} - Listar documentos de uma marcação
 * - GET /api/documentos/{id}/download - Download de documento
 * - DELETE /api/documentos/{id} - Remover documento
 */
@Slf4j
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;
    private final AuthorizationService authorizationService;
    private final MarcacaoRepository marcacaoRepository;
    private final pt.florinhas.marcacoes.service.AuditLogService auditLogService;

    /**
     * Upload de um documento para uma marcação.
     * 
     * Apenas o utente dono da marcação ou funcionários podem fazer upload.
     * 
     * @param marcacaoId ID da marcação
     * @param file ficheiro a fazer upload
     * @return DTO com dados do documento criado
     * @throws IOException se houver erro ao processar o ficheiro
     */
    @PostMapping("/marcacao/{marcacaoId}/upload")
    public ResponseEntity<DocumentoDTO> uploadDocumento(
            @PathVariable Long marcacaoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "finalidade", required = false) String finalidade) throws IOException {
        
        log.info("Recebido pedido de upload de documento para marcação {}", marcacaoId);

        // Verificar permissões
        verificarPermissaoMarcacao(marcacaoId);

        DocumentoDTO documento = documentoService.uploadDocumento(marcacaoId, file, finalidade);
        
        auditLogService.log(
            "UPLOAD_DOCUMENTO",
            "DOCUMENTO",
            documento.id(),
            String.format("Upload: %s (Marcação: %d, Finalidade: %s)", 
                documento.nomeOriginal(), marcacaoId, finalidade != null ? finalidade : "N/A")
        );
        
        return ResponseEntity.ok(documento);
    }

    /**
     * Lista todos os documentos de uma marcação.
     * 
     * @param marcacaoId ID da marcação
     * @return lista de documentos
     */
    @GetMapping("/marcacao/{marcacaoId}")
    public ResponseEntity<List<DocumentoDTO>> listarDocumentos(@PathVariable Long marcacaoId) {
        log.info("Listando documentos da marcação {}", marcacaoId);

        // Verificar permissões
        verificarPermissaoMarcacao(marcacaoId);

        List<DocumentoDTO> documentos = documentoService.listarDocumentosDaMarcacao(marcacaoId);
        
        return ResponseEntity.ok(documentos);
    }

    /**
     * Pesquisa documentos por metadados.
     *
     * Regras de acesso:
     * - com marcacaoId: utente dono da marcação ou secretaria
     * - sem marcacaoId: apenas secretaria
     */
    @GetMapping("/pesquisar")
    public ResponseEntity<List<DocumentoDTO>> pesquisarDocumentosPorMetadados(
            @RequestParam(required = false) Long marcacaoId,
            @RequestParam(required = false) String nomeOriginal,
            @RequestParam(required = false) String nomeArmazenado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String utenteNome,
            @RequestParam(required = false) String utenteNif,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime marcacaoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime marcacaoAte) {

        if (marcacaoId != null) {
            verificarPermissaoMarcacao(marcacaoId);
        } else if (!isSecretariaOuStaff()) {
            throw new AccessDeniedException("Pesquisa global de documentos requer perfil de secretaria");
        }

        List<DocumentoDTO> resultados = documentoService.pesquisarDocumentosPorMetadados(
            marcacaoId,
            nomeOriginal,
            nomeArmazenado,
            tipo,
            utenteNome,
            utenteNif,
            marcacaoDesde,
            marcacaoAte
        );

        return ResponseEntity.ok(resultados);
    }

    /**
     * Notifica o utente de que os documentos enviados são inválidos.
     * @param marcacaoId ID da marcação
     * @param observacoes Observações/motivo da invalidação
     */
    @PostMapping("/marcacao/{marcacaoId}/notificar-invalidos")
    public ResponseEntity<Void> notificarDocumentoInvalido(
            @PathVariable Long marcacaoId,
            @RequestParam("documentoId") Long documentoId,
            @RequestParam("observacoes") String observacoes) {
        documentoService.notificarDocumentoInvalido(marcacaoId, documentoId, observacoes);
        return ResponseEntity.ok().build();
    }

    private boolean isSecretariaOuStaff() {
        return authorizationService.hasAnyRole("ROLE_SECRETARIA", "ROLE_FUNCIONARIO");
    }

    /**
     * Download de um documento.
     * 
     * @param id ID do documento
     * @return ficheiro para download
     */

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocumento(@PathVariable Long id) {
        log.info("Download de documento {}", id);

        DocumentoDTO documentoDTO = documentoService.obterDocumento(id);
        // Verificar permissões para a marcação associada
        verificarPermissaoMarcacao(documentoDTO.marcacaoId());

        auditLogService.log(
            "DOWNLOAD_DOCUMENTO",
            "DOCUMENTO",
            id,
            String.format("Download: %s (Marcação: %d)", documentoDTO.nomeOriginal(), documentoDTO.marcacaoId())
        );

        Resource resource = documentoService.carregarFicheiro(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(documentoDTO.tipo()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + documentoDTO.nomeOriginal() + "\"")
            .body(resource);
    }

    /**
     * Preview inline de um documento (para visualização direta no navegador).
     * @param id ID do documento
     * @return ficheiro para visualização inline
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewDocumento(@PathVariable Long id) {
        log.info("Preview inline de documento {}", id);

        DocumentoDTO documentoDTO = documentoService.obterDocumento(id);
        verificarPermissaoMarcacao(documentoDTO.marcacaoId());

        auditLogService.log(
            "PREVIEW_DOCUMENTO",
            "DOCUMENTO",
            id,
            String.format("Preview: %s (Marcação: %d)", documentoDTO.nomeOriginal(), documentoDTO.marcacaoId())
        );

        Resource resource = documentoService.carregarFicheiro(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(documentoDTO.tipo()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "inline; filename=\"" + documentoDTO.nomeOriginal() + "\"")
            .body(resource);
    }

    /**
     * Obtém metadados completos de um documento.
     *
     * @param id ID do documento
     * @return metadados do documento
     */
    @GetMapping("/{id}/metadata")
    public ResponseEntity<DocumentoMetadataDTO> obterMetadadosDocumento(@PathVariable Long id) {
        log.info("Obtendo metadados do documento {}", id);

        DocumentoDTO documentoDTO = documentoService.obterDocumento(id);
        verificarPermissaoMarcacao(documentoDTO.marcacaoId());

        DocumentoMetadataDTO metadata = documentoService.obterMetadadosDocumento(id);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Remove um documento.
     * 
     * @param id ID do documento
     * @return resposta vazia de sucesso
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerDocumento(@PathVariable Long id) {
        log.info("Removendo documento {}", id);

        DocumentoDTO documentoDTO = documentoService.obterDocumento(id);
        
        // Verificar permissões para a marcação associada
        verificarPermissaoMarcacao(documentoDTO.marcacaoId());

        auditLogService.log(
            "DELETE_DOCUMENTO",
            "DOCUMENTO",
            id,
            String.format("Documento removido: %s (Marcação: %d)", 
                documentoDTO.nomeOriginal(), documentoDTO.marcacaoId())
        );

        documentoService.removerDocumento(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se o utilizador atual tem permissão para aceder a uma marcação.
     * 
     * Permissões:
     * - Admin: sempre permitido
     * - Funcionário/Secretaria: sempre permitido
     * - Utente: apenas se for o dono da marcação
     * 
     * @param marcacaoId ID da marcação
     * @throws AccessDeniedException se o utilizador não tiver permissão
     */
    private void verificarPermissaoMarcacao(Long marcacaoId) {
        Long currentUserId = authorizationService.getCurrentUserId();
        boolean isAdmin = authorizationService.isAdmin();

        // Admin e funcionários têm acesso total
        if (isAdmin) {
            return;
        }

        // Para utentes, verificar se é o dono da marcação
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
            .orElseThrow(() -> new AccessDeniedException("Marcação não encontrada"));

        Long ownerId = null;
        if (marcacao.getMarcacaoSecretaria() != null && 
            marcacao.getMarcacaoSecretaria().getUtente() != null) {
            ownerId = marcacao.getMarcacaoSecretaria().getUtente().getId();
        } else if (marcacao.getCriadoPor() != null) {
            ownerId = marcacao.getCriadoPor().getId();
        }

        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("Sem permissão para aceder a esta marcação");
        }
    }
}
