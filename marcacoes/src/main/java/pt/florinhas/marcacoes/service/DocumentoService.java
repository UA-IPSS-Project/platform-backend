package pt.florinhas.marcacoes.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.NotificacaoTipo;

/**
 * Serviço responsável pela gestão de documentos anexados a marcações.
 * 
 * Funcionalidades:
 * - Upload de documentos com validação de tipo e tamanho
 * - Armazenamento seguro em MinIO
 * - Recuperação de documentos para download
 * - Remoção de documentos
 */
@Service
@Slf4j
@RequiredArgsConstructor

public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final MarcacaoRepository marcacaoRepository;
    private final MinioClient minioClient;
    private final FuncionarioRepository funcionarioRepository;
    private final NotificacaoService notificacaoService;

    /**
     * Bucket MinIO onde os documentos são armazenados.
     */
    @Value("${minio.bucket:marcacoes}")
    private String bucketName;

    /**
     * Tamanho máximo permitido para upload (em bytes).
     * Default: 10MB
     */
    @Value("${app.upload.max-size:10485760}")
    private Long maxFileSize;

    /**
     * Tipos MIME permitidos para upload.
     */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Faz upload de um documento para uma marcação específica.
     * 
     * @param marcacaoId ID da marcação
     * @param file ficheiro a fazer upload
     * @return DTO com dados do documento criado
     * @throws ResourceNotFoundException se a marcação não existir
     * @throws IllegalArgumentException se o ficheiro for inválido
     * @throws IOException se houver erro ao guardar o ficheiro
     */
    @Transactional

    public DocumentoDTO uploadDocumento(Long marcacaoId, MultipartFile file) throws IOException {

        log.info("Iniciando upload de documento para marcação {}", marcacaoId);

        // Validar marcação
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Marcação não encontrada com ID: " + marcacaoId));

        // Limite de 10 ficheiros por marcação
        Long ficheirosExistentes = documentoRepository.countByMarcacaoId(marcacaoId);
        if (ficheirosExistentes != null && ficheirosExistentes >= 10) {
            throw new IllegalArgumentException("Limite máximo de 10 ficheiros por marcação atingido.");
        }

        // Validações do ficheiro
        validarFicheiro(file);


        // Obter NIF do utente associado à marcação (via MarcacaoSecretaria)
        String nif = null;
        if (marcacao.getMarcacaoSecretaria() != null && marcacao.getMarcacaoSecretaria().getUtente() != null) {
            nif = marcacao.getMarcacaoSecretaria().getUtente().getNif();
        } else {
            // fallback: usar criador da marcação
            nif = marcacao.getCriadoPor() != null ? marcacao.getCriadoPor().getNif() : "SEM_NIF";
        }

        // Gerar nome original no padrão NIF_ASSUNTO_DATA_UUID.extensão
        String extensao = obterExtensao(file.getOriginalFilename());
        String assunto = "SEM_ASSUNTO";
        String dataMarcacao = "00000000";

        if (marcacao.getData() != null) {
            dataMarcacao = marcacao.getData().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        if (marcacao.getMarcacaoSecretaria() != null && marcacao.getMarcacaoSecretaria().getAssunto() != null) {
            assunto = sanitizarNome(marcacao.getMarcacaoSecretaria().getAssunto());
        } else if (marcacao.getMarcacaoBalneario() != null) {
            assunto = "BALNEARIO";
        }

        // Obter a próxima sequência para esta marcação
        Integer proximaSequencia = documentoRepository.findMaxSequenciaByMarcacaoId(marcacaoId).orElse(0) + 1;

        String nomeOriginal = String.format("%s_%s_%d_%s%s",
            nif != null ? nif : "SEM_NIF",
            assunto,
            proximaSequencia,
            dataMarcacao,
            extensao
        );

        // Nome armazenado passa a ser determinístico por marcação: M{ID}_D{SEQ}.extensao
        String nomeArmazenado = String.format("M%d_D%d%s", marcacaoId, proximaSequencia, extensao);

        // Criar diretório organizado por ano/mês
        LocalDate hoje = LocalDate.now();
        String caminhoRelativo = String.format("%d/%02d", hoje.getYear(), hoje.getMonthValue());
        String objectName = caminhoRelativo + "/" + nomeArmazenado;
        String tipo = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            garantirBucketExiste();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(tipo)
                    .build()
            );
        } catch (Exception e) {
            throw new IOException("Erro ao guardar ficheiro no MinIO", e);
        }

        log.info("Ficheiro salvo no MinIO: {}", objectName);

        // Criar entidade Documento
        Documento documento = new Documento();
        documento.setNomeOriginal(nomeOriginal);
        documento.setNomeArmazenado(nomeArmazenado);
        documento.setCaminho(objectName);
        documento.setTipo(tipo);
        documento.setTamanho(file.getSize());
        documento.setMarcacao(marcacao);
        documento.setSequencia(proximaSequencia);

        // Salvar no banco de dados
        Documento documentoSalvo = documentoRepository.save(documento);


        log.info("Documento {} salvo com sucesso para marcação {}", documentoSalvo.getId(), marcacaoId);

        // Notificar secretarias apenas se o criador da marcação for utente (não funcionário/secretaria)
        Utilizador criador = marcacao.getCriadoPor();
        if (criador != null && criador.getClass().getSimpleName().equals("Utente")) {
            try {
                List<Funcionario> secretarias = funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA);
                String titulo = "Novo documento enviado";
                String mensagem = String.format("Um novo documento foi enviado para a marcação #%d.", marcacaoId);
                for (Funcionario secretaria : secretarias) {
                    notificacaoService.criarNotificacao(
                        secretaria.getId(),
                        titulo,
                        mensagem,
                        NotificacaoTipo.FICHEIRO
                    );
                }
            } catch (Exception e) {
                log.error("Erro ao notificar secretarias sobre upload de documento", e);
            }
        }

        return DocumentoDTO.fromDocumento(documentoSalvo);
    }

    /**
     * Lista todos os documentos de uma marcação.
     * 
     * @param marcacaoId ID da marcação
     * @return lista de DTOs de documentos
     */
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarDocumentosDaMarcacao(Long marcacaoId) {
        log.info("Listando documentos da marcação {}", marcacaoId);
        
        List<Documento> documentos = documentoRepository.findByMarcacaoId(marcacaoId);
        return documentos.stream()
            .map(DocumentoDTO::fromDocumento)
            .toList();
    }

    /**
     * Pesquisa documentos por metadados com filtros opcionais.
     *
     * @param marcacaoId ID da marcação
     * @param nomeOriginal parte do nome original
     * @param nomeArmazenado parte do nome armazenado
     * @param tipo tipo MIME
    * @param utenteNome parte do nome do utente associado
    * @param utenteNif parte do NIF do utente associado
     * @param uploadedDesde data/hora inicial de upload
     * @param uploadedAte data/hora final de upload
     * @return lista de documentos encontrados
     */
    @Transactional(readOnly = true)
    public List<DocumentoDTO> pesquisarDocumentosPorMetadados(
        Long marcacaoId,
        String nomeOriginal,
        String nomeArmazenado,
        String tipo,
        String utenteNome,
        String utenteNif,
        LocalDateTime marcacaoDesde,
        LocalDateTime marcacaoAte
    ) {
        if (marcacaoDesde != null && marcacaoAte != null && marcacaoDesde.isAfter(marcacaoAte)) {
            throw new IllegalArgumentException("marcacaoDesde não pode ser posterior a marcacaoAte");
        }

        log.info("Pesquisa de documentos por metadados (marcacaoId={}, tipo={})", marcacaoId, tipo);

        List<Documento> documentosBase = obterDocumentosPorIntervalo(marcacaoId, marcacaoDesde, marcacaoAte);

        return documentosBase
            .stream()
            .filter(documento -> {
                if (nomeOriginal == null || nomeOriginal.isBlank()) {
                    return true;
                }
                String valor = documento.getNomeOriginal();
                return valor != null && valor.toLowerCase(Locale.ROOT).contains(nomeOriginal.toLowerCase(Locale.ROOT));
            })
            .filter(documento -> {
                if (nomeArmazenado == null || nomeArmazenado.isBlank()) {
                    return true;
                }
                String valor = documento.getNomeArmazenado();
                return valor != null && valor.toLowerCase(Locale.ROOT).contains(nomeArmazenado.toLowerCase(Locale.ROOT));
            })
            .filter(documento -> {
                if (tipo == null || tipo.isBlank()) {
                    return true;
                }
                String valor = documento.getTipo();
                return valor != null && valor.equalsIgnoreCase(tipo);
            })
            .filter(documento -> {
                if (utenteNome == null || utenteNome.isBlank()) {
                    return true;
                }
                String nome = obterNomeUtenteMarcacao(documento.getMarcacao());
                return nome != null && nome.toLowerCase(Locale.ROOT).contains(utenteNome.toLowerCase(Locale.ROOT));
            })
            .filter(documento -> {
                if (utenteNif == null || utenteNif.isBlank()) {
                    return true;
                }
                String nif = obterNifUtenteMarcacao(documento.getMarcacao());
                return nif != null && nif.contains(utenteNif);
            })
            .map(DocumentoDTO::fromDocumento)
            .toList();
    }

    private List<Documento> obterDocumentosPorIntervalo(
        Long marcacaoId,
        LocalDateTime marcacaoDesde,
        LocalDateTime marcacaoAte
    ) {
        // Com marcacaoId: filtrar dentro desse ID (queries existentes por uploadedEm quando sem datas,
        // ou por data da marcação quando com datas)
        if (marcacaoId != null) {
            if (marcacaoDesde != null && marcacaoAte != null) {
                return documentoRepository.findByMarcacaoIdAndMarcacaoDataBetween(marcacaoId, marcacaoDesde, marcacaoAte);
            }
            // sem intervalo de datas: devolver todos os documentos da marcação
            return documentoRepository.findByMarcacaoIdOrderByUploadedEmDesc(marcacaoId);
        }

        // Sem marcacaoId: filtrar globalmente pela data da marcação
        if (marcacaoDesde != null && marcacaoAte != null) {
            return documentoRepository.findByMarcacaoDataBetween(marcacaoDesde, marcacaoAte);
        }
        if (marcacaoDesde != null) {
            return documentoRepository.findByMarcacaoDataGreaterThanEqual(marcacaoDesde);
        }
        if (marcacaoAte != null) {
            return documentoRepository.findByMarcacaoDataLessThanEqual(marcacaoAte);
        }
        return documentoRepository.findAllByOrderByUploadedEmDesc();
    }

    /**
     * Obtém um documento pelo ID.
     * 
     * @param documentoId ID do documento
     * @return DTO do documento
     * @throws ResourceNotFoundException se o documento não existir
     */
    @Transactional(readOnly = true)
    public DocumentoDTO obterDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + documentoId));
        
        return DocumentoDTO.fromDocumento(documento);
    }

    /**
     * Obtém metadados completos de um documento (BD + MinIO).
     *
     * @param documentoId ID do documento
     * @return DTO com metadados detalhados
     */
    @Transactional(readOnly = true)
    public DocumentoMetadataDTO obterMetadadosDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + documentoId));

        try {
            StatObjectResponse statObject = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(documento.getCaminho())
                    .build()
            );

            Map<String, String> minioUserMetadata = statObject.userMetadata() != null
                ? statObject.userMetadata()
                : Collections.emptyMap();

            String minioLastModified = statObject.lastModified() != null
                ? statObject.lastModified().toString()
                : null;

            return new DocumentoMetadataDTO(
                documento.getId(),
                documento.getNomeOriginal(),
                documento.getNomeArmazenado(),
                documento.getCaminho(),
                documento.getTipo(),
                documento.getTamanho(),
                documento.getUploadedEm(),
                documento.getMarcacao().getId(),
                statObject.etag(),
                minioLastModified,
                minioUserMetadata,
                documento.getSequencia()
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Erro ao obter metadados do documento: " + e.getMessage());
        }
    }

    /**
     * Carrega um ficheiro para download.
     * 
     * @param documentoId ID do documento
     * @return Resource com o conteúdo do ficheiro
     * @throws ResourceNotFoundException se o documento não existir ou ficheiro não for encontrado
     */
    @Transactional(readOnly = true)
    public Resource carregarFicheiro(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + documentoId));

        try {
            GetObjectResponse objeto = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(documento.getCaminho())
                    .build()
            );
            return new InputStreamResource(objeto);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Erro ao carregar ficheiro: " + documento.getNomeOriginal() + ", " + e.getMessage());
        }
    }

    /**
     * Remove um documento (ficheiro e registo na BD).
     * 
     * @param documentoId ID do documento a remover
     * @throws ResourceNotFoundException se o documento não existir
     */
    @Transactional
    public void removerDocumento(Long documentoId) {
        log.info("Removendo documento {}", documentoId);

        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + documentoId));

        // Remover registo da base de dados
        documentoRepository.delete(documento);
        log.info("Documento {} removido com sucesso", documentoId);
    }

        /**
         * Notifica o utente de que um documento enviado é inválido.
         * @param marcacaoId ID da marcação
         * @param documentoId ID do documento inválido
         * @param motivo Observações/motivo da invalidação
         */
        @Transactional
        public void notificarDocumentoInvalido(Long marcacaoId, Long documentoId, String motivo) {
            Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Marcação não encontrada com ID: " + marcacaoId));
            Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + documentoId));
            Utilizador utente = null;
            if (marcacao.getMarcacaoSecretaria() != null && marcacao.getMarcacaoSecretaria().getUtente() != null) {
                utente = marcacao.getMarcacaoSecretaria().getUtente();
            } else if (marcacao.getCriadoPor() != null) {
                utente = marcacao.getCriadoPor();
            }
            if (utente == null) {
                throw new IllegalArgumentException("Não foi possível identificar o utente para notificação.");
            }
            String titulo = "Documento inválido";
            String dataMarcacao = marcacao.getData() != null ? marcacao.getData().toLocalDate().toString() : "(data desconhecida)";
            String nomeDoc = documento.getNomeOriginal();
            String mensagem = String.format("Na marcação do dia %s, o documento '%s' é inválido.%s", dataMarcacao, nomeDoc, (motivo != null && !motivo.isBlank() ? " Motivo: " + motivo : ""));
            notificacaoService.criarNotificacao(utente.getId(), titulo, mensagem, NotificacaoTipo.DOCUMENTO_INVALIDO);
        }

    /**
     * Valida se o ficheiro atende aos critérios de upload.
     * 
     * @param file ficheiro a validar
     * @throws IllegalArgumentException se o ficheiro for inválido
     */
    private void validarFicheiro(MultipartFile file) {
        // Verificar se o ficheiro está vazio
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Ficheiro vazio não é permitido");
        }

        // Verificar tamanho
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                String.format("Ficheiro excede o tamanho máximo permitido de %d MB", maxFileSize / (1024 * 1024))
            );
        }

        // Verificar tipo MIME
        String tipo = file.getContentType();
        if (tipo == null || !ALLOWED_MIME_TYPES.contains(tipo)) {
            throw new IllegalArgumentException(
                "Tipo de ficheiro não permitido. Tipos aceites: PDF, JPEG, PNG, DOC, DOCX"
            );
        }

        // Verificar se tem nome
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            throw new IllegalArgumentException("Nome do ficheiro inválido");
        }
    }

    /**
     * Extrai a extensão do nome do ficheiro.
     * 
     * @param nomeOriginal nome original do ficheiro
     * @return extensão com ponto (ex: ".pdf") ou string vazia
     */
    private String obterExtensao(String nomeOriginal) {
        if (nomeOriginal == null) {
            return "";
        }
        int pontoIndex = nomeOriginal.lastIndexOf('.');
        return pontoIndex > 0 ? nomeOriginal.substring(pontoIndex) : "";
    }

    private void garantirBucketExiste() throws Exception {
        boolean bucketExiste = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucketName)
                .build()
        );

        if (!bucketExiste) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            log.info("Bucket MinIO criado automaticamente: {}", bucketName);
        }
    }

    private String obterNomeUtenteMarcacao(Marcacao marcacao) {
        Utilizador utente = obterUtenteMarcacao(marcacao);
        return utente != null ? utente.getNome() : null;
    }

    private String obterNifUtenteMarcacao(Marcacao marcacao) {
        Utilizador utente = obterUtenteMarcacao(marcacao);
        return utente != null ? utente.getNif() : null;
    }

    private Utilizador obterUtenteMarcacao(Marcacao marcacao) {
        if (marcacao == null) {
            return null;
        }

        if (marcacao.getMarcacaoSecretaria() != null && marcacao.getMarcacaoSecretaria().getUtente() != null) {
            return marcacao.getMarcacaoSecretaria().getUtente();
        }

        if (marcacao.getCriadoPor() != null) {
            return marcacao.getCriadoPor();
        }

        return null;
    }

    private String sanitizarNome(String nome) {
        if (nome == null) return "SEM_ASSUNTO";
        return nome.trim()
            .replaceAll("[\\s/\\\\:*?\"<>|]", "_") // Substituir caracteres inválidos e espaços por underscore
            .replaceAll("_+", "_") // Remover underscores duplicados
            .toUpperCase();
    }
}
