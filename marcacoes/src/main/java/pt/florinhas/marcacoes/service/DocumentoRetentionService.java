package pt.florinhas.marcacoes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.repository.DocumentoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentoRetentionService {

    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;
    private final AuditLogService auditLogService;

    /**
     * Job executado diariamente às 2h da manhã para remover documentos expirados.
     * Cron: segundo minuto hora dia mês dia-da-semana
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void limparDocumentosExpirados() {
        log.info("Iniciando limpeza de documentos expirados");

        try {
            LocalDateTime agora = LocalDateTime.now();
            List<Documento> documentosExpirados = documentoRepository
                .findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(agora);

            if (documentosExpirados.isEmpty()) {
                log.info("Nenhum documento expirado encontrado");
                return;
            }

            log.info("Encontrados {} documentos expirados para remoção", documentosExpirados.size());

            int removidos = 0;
            int erros = 0;

            for (Documento doc : documentosExpirados) {
                try {
                    log.debug("Removendo documento expirado: ID={}, Nome={}, Expiração={}", 
                        doc.getId(), doc.getNomeOriginal(), doc.getDataExpiracao());

                    documentoService.removerDocumento(doc.getId());

                    auditLogService.log(
                        "LIMPEZA_AUTOMATICA",
                        "DOCUMENTO",
                        doc.getId(),
                        String.format("Documento removido automaticamente (expirado em %s): %s", 
                            doc.getDataExpiracao(), doc.getNomeOriginal())
                    );

                    removidos++;
                } catch (Exception e) {
                    log.error("Erro ao remover documento ID {}: {}", doc.getId(), e.getMessage(), e);
                    erros++;
                }
            }

            log.info("Limpeza concluída: {} documentos removidos, {} erros", removidos, erros);

        } catch (Exception e) {
            log.error("Erro durante limpeza de documentos expirados: {}", e.getMessage(), e);
        }
    }
}
