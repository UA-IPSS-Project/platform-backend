package pt.florinhas.marcacoes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encriptação AES-256-GCM de documentos antes de enviar para o MinIO.
 *
 * Formato armazenado: [ IV (12 bytes) | ciphertext + GCM tag (n+16 bytes) ]
 * O IV é gerado aleatoriamente por cada upload — o mesmo ficheiro produz
 * ciphertexts diferentes, impedindo correlação.
 *
 * AES-GCM requer o plaintext completo em memória para calcular o tag de
 * autenticação. Para o limite de 10 MB configurado em app.upload.max-size
 * este comportamento é aceitável. O tamanho é validado antes de ler para
 * evitar OOM em uploads maliciosos.
 */
@Service
public class DocumentoEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final long maxFileSize;

    public DocumentoEncryptionService(
            @Value("${document.encryption.key}") String base64Key,
            @Value("${app.upload.max-size:10485760}") long maxFileSize) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "DOCUMENT_ENCRYPTION_KEY deve ser uma chave Base64 de 32 bytes (AES-256)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.maxFileSize = maxFileSize;
    }

    public EncryptedResult encrypt(InputStream plaintext, long declaredSize) throws Exception {
        if (declaredSize > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("Ficheiro excede o tamanho máximo de %d MB", maxFileSize / (1024 * 1024)));
        }
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.readAllBytes());
        byte[] blob = ByteBuffer.allocate(IV_LENGTH + ciphertext.length).put(iv).put(ciphertext).array();
        return new EncryptedResult(new ByteArrayInputStream(blob), blob.length);
    }

    public InputStream decrypt(InputStream encryptedStream) throws Exception {
        byte[] blob;
        try (encryptedStream) {
            blob = encryptedStream.readAllBytes();
        }
        if (blob.length < IV_LENGTH + TAG_LENGTH_BITS / 8) {
            throw new IllegalArgumentException("Blob encriptado demasiado curto — dados corrompidos ou inválidos");
        }
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(blob, 0, iv, 0, IV_LENGTH);
        byte[] ciphertext = new byte[blob.length - IV_LENGTH];
        System.arraycopy(blob, IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        return new ByteArrayInputStream(cipher.doFinal(ciphertext));
    }

    public record EncryptedResult(InputStream stream, long size) {}
}
