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
 * Formato do blob encriptado: [ IV (12 bytes) | ciphertext+tag (n+16 bytes) ]
 * O IV é gerado aleatoriamente por cada upload (nunca reutilizado).
 */
@Service
public class DocumentoEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;

    public DocumentoEncryptionService(@Value("${document.encryption.key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("DOCUMENT_ENCRYPTION_KEY deve ser uma chave Base64 de 32 bytes (AES-256)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /** Encripta os bytes do ficheiro. Devolve InputStream com [ IV | ciphertext+tag ]. */
    public EncryptedResult encrypt(InputStream plaintext, long size) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

        byte[] plaintextBytes = plaintext.readAllBytes();
        byte[] ciphertext = cipher.doFinal(plaintextBytes);

        byte[] blob = ByteBuffer.allocate(IV_LENGTH + ciphertext.length)
                .put(iv)
                .put(ciphertext)
                .array();

        return new EncryptedResult(new ByteArrayInputStream(blob), blob.length);
    }

    /** Desencripta o blob lido do MinIO e devolve o conteúdo original. */
    public InputStream decrypt(InputStream encryptedStream) throws Exception {
        byte[] blob = encryptedStream.readAllBytes();

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
