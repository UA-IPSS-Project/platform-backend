package pt.florinhas.common_data.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA AttributeConverter que encripta/desencripta campos String com AES-256-GCM.
 *
 * Formato armazenado na BD (Base64): [ IV (12 bytes) | ciphertext + tag (n+16 bytes) ]
 * O IV é gerado aleatoriamente em cada escrita — o mesmo valor em claro produz
 * ciphertexts diferentes, impedindo correlação entre registos.
 *
 * Nota: como o IV é aleatório, a unicidade do campo deve ser garantida pelo
 * nif_hash (SHA-256), não pelo campo encriptado.
 */
@Component
@Converter
public class AesEncryptorConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private static byte[] keyBytes;

    @Value("${NIF_ENCRYPTION_KEY:florinhas_dev_secret_key_32bytes_!}")
    public void setEncryptionKey(String key) {
        byte[] raw = key.getBytes();
        keyBytes = new byte[32];
        System.arraycopy(raw, 0, keyBytes, 0, Math.min(raw.length, 32));
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(attribute.getBytes());

            byte[] blob = ByteBuffer.allocate(IV_LENGTH + ciphertext.length).put(iv).put(ciphertext).array();
            return Base64.getEncoder().encodeToString(blob);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar dado", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] blob = Base64.getDecoder().decode(dbData);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(blob, 0, iv, 0, IV_LENGTH);
            byte[] ciphertext = new byte[blob.length - IV_LENGTH];
            System.arraycopy(blob, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desencriptar dado", e);
        }
    }
}
