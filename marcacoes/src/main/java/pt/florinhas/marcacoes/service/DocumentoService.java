package pt.florinhas.marcacoes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

/**
 * Serviço responsável pela gestão de documentos anexados a marcações.
 * 
 * Funcionalidades:
 * - Upload de documentos com validação de tipo e tamanho
 * - Armazenamento seguro no sistema de ficheiros
 * - Recuperação de documentos para download
 * - Remoção de documentos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final MarcacaoRepository marcacaoRepository;

    /**
     * Diretório base onde os documentos são armazenados.
     * Configurável via application.properties
     */
    @Value("${app.upload.dir:uploads/documentos}")
    private String uploadDir;

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

        // Validações do ficheiro
        validarFicheiro(file);

        // Gerar nome único para o ficheiro
        String nomeOriginal = file.getOriginalFilename();
        String extensao = obterExtensao(nomeOriginal);
        String nomeArmazenado = UUID.randomUUID().toString() + extensao;

        // Criar diretório organizado por ano/mês
        LocalDate hoje = LocalDate.now();
        String caminhoRelativo = String.format("%d/%02d", hoje.getYear(), hoje.getMonthValue());
        Path diretorioDestino = Paths.get(uploadDir, caminhoRelativo);
        
        // Criar diretórios se não existirem
        Files.createDirectories(diretorioDestino);

        // Caminho completo do ficheiro
        Path caminhoCompleto = diretorioDestino.resolve(nomeArmazenado);

        // Copiar ficheiro para o destino
        Files.copy(file.getInputStream(), caminhoCompleto, StandardCopyOption.REPLACE_EXISTING);

        log.info("Ficheiro salvo em: {}", caminhoCompleto);

        // Criar entidade Documento
        Documento documento = new Documento();
        documento.setNomeOriginal(nomeOriginal);
        documento.setNomeArmazenado(nomeArmazenado);
        documento.setCaminho(caminhoRelativo + "/" + nomeArmazenado);
        documento.setTipo(file.getContentType());
        documento.setTamanho(file.getSize());
        documento.setMarcacao(marcacao);

        // Salvar no banco de dados
        Documento documentoSalvo = documentoRepository.save(documento);

        log.info("Documento {} salvo com sucesso para marcação {}", documentoSalvo.getId(), marcacaoId);

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
            Path caminhoFicheiro = Paths.get(uploadDir).resolve(documento.getCaminho());
            Resource resource = new UrlResource(caminhoFicheiro.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Ficheiro não encontrado ou não legível: " + documento.getNomeOriginal());
            }
        } catch (IOException e) {
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

        // Remover ficheiro do sistema de ficheiros
        try {
            Path caminhoFicheiro = Paths.get(uploadDir).resolve(documento.getCaminho());
            Files.deleteIfExists(caminhoFicheiro);
            log.info("Ficheiro removido: {}", caminhoFicheiro);
        } catch (IOException e) {
            log.error("Erro ao remover ficheiro: {}", documento.getCaminho(), e);
            // Continua com a remoção do registo mesmo se o ficheiro não for encontrado
        }

        // Remover registo da base de dados
        documentoRepository.delete(documento);
        log.info("Documento {} removido com sucesso", documentoId);
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
        String tipoMime = file.getContentType();
        if (tipoMime == null || !ALLOWED_MIME_TYPES.contains(tipoMime)) {
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
}
