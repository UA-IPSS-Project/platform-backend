package pt.florinhas.common_data.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Component
@Converter
public class AesEncryptorConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final byte[] keyBytes;

    public AesEncryptorConverter(Environment env) {
        String key = env.getProperty("NIF_ENCRYPTION_KEY", "florinhas_dev_secret_key_32bytes_!");
        byte[] raw = key.getBytes();
        this.keyBytes = new byte[32];
        System.arraycopy(raw, 0, this.keyBytes, 0, Math.min(raw.length, 32));
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        Objects.requireNonNull(keyBytes, "AesEncryptorConverter: chave de encriptação não inicializada");
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
        Objects.requireNonNull(keyBytes, "AesEncryptorConverter: chave de encriptação não inicializada");
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
