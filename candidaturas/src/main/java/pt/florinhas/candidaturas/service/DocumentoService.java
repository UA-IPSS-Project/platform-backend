package pt.florinhas.candidaturas.service;

import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import pt.florinhas.candidaturas.domain.CandidaturaDocumento;
import pt.florinhas.candidaturas.dto.CandidaturaDocumentoDTO;
import pt.florinhas.candidaturas.repository.CandidaturaDocumentoRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentoService {

    private final MinioClient minioClient;
    private final CandidaturaDocumentoRepository documentoRepository;

    @Value("${minio.bucket:candidaturas}")
    private String bucketName;

    @Value("${app.upload.max-size:10485760}")
    private Long maxFileSize;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    public CandidaturaDocumentoDTO uploadDocumento(String candidaturaId, MultipartFile file, Long userId) {
        validarFicheiro(file);

        long count = documentoRepository.findByCandidaturaId(candidaturaId).size();
        if (count >= 10) {
            throw new IllegalArgumentException("Limite máximo de 10 ficheiros por candidatura atingido.");
        }

        String extensao = obterExtensao(file.getOriginalFilename());
        String nomeArmazenado = UUID.randomUUID().toString() + extensao;
        String minioKey = candidaturaId + "/" + nomeArmazenado;

        try {
            garantirBucket();
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(minioKey)
                        .stream(is, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }
        } catch (Exception e) {
            log.error("Erro ao fazer upload para MinIO: {}", e.getMessage());
            throw new RuntimeException("Erro ao guardar o ficheiro.", e);
        }

        CandidaturaDocumento doc = new CandidaturaDocumento();
        doc.setCandidaturaId(candidaturaId);
        doc.setMinioKey(minioKey);
        doc.setNomeOriginal(file.getOriginalFilename());
        doc.setNomeArmazenado(nomeArmazenado);
        doc.setTipo(file.getContentType());
        doc.setTamanho(file.getSize());
        doc.setUploadedEm(Instant.now());
        doc.setUploadedPor(userId);

        return CandidaturaDocumentoDTO.fromEntity(documentoRepository.save(doc));
    }

    public List<CandidaturaDocumentoDTO> listarDocumentos(String candidaturaId) {
        return documentoRepository.findByCandidaturaId(candidaturaId).stream()
                .map(CandidaturaDocumentoDTO::fromEntity)
                .toList();
    }

    public Resource downloadDocumento(String documentoId) {
        CandidaturaDocumento doc = documentoRepository.findById(documentoId).orElse(null);
        if (doc == null) return null;

        try {
            InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(doc.getMinioKey())
                    .build());
            return new InputStreamResource(is);
        } catch (Exception e) {
            log.error("Erro ao descarregar ficheiro do MinIO: {}", e.getMessage());
            throw new RuntimeException("Erro ao carregar o ficheiro.", e);
        }
    }

    public String getTipo(String documentoId) {
        return documentoRepository.findById(documentoId).map(CandidaturaDocumento::getTipo).orElse(null);
    }

    public String getNomeOriginal(String documentoId) {
        return documentoRepository.findById(documentoId).map(CandidaturaDocumento::getNomeOriginal).orElse(null);
    }

    public boolean removerDocumento(String documentoId) {
        CandidaturaDocumento doc = documentoRepository.findById(documentoId).orElse(null);
        if (doc == null) return false;

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(doc.getMinioKey())
                    .build());
        } catch (Exception e) {
            log.warn("Erro ao remover ficheiro do MinIO (continuando remoção do registo): {}", e.getMessage());
        }

        documentoRepository.deleteById(documentoId);
        return true;
    }

    private void garantirBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket '{}' criado.", bucketName);
        }
    }

    private void validarFicheiro(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Ficheiro inválido ou vazio.");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Ficheiro excede o tamanho máximo de 10MB.");
        }
        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME_TYPES.contains(mime)) {
            throw new IllegalArgumentException("Tipo de ficheiro não permitido: " + mime);
        }
    }

    private String obterExtensao(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
