package pt.florinhas.common_data.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Converter
public class AesEncryptorConverter implements AttributeConverter<String, String> {

    private static String encryptionKey;

    @Value("${NIF_ENCRYPTION_KEY:florinhas_dev_secret_key_32bytes_!}")
    public void setEncryptionKey(String key) {
        AesEncryptorConverter.encryptionKey = key;
    }

    private static final String AES = "AES";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null)
            return null;
        try {
            Cipher cipher = Cipher.getInstance(AES);
            // PAD THE KEY OR ENSURE IT'S 32 BYTES
            byte[] keyBytes = new byte[32];
            byte[] originalBytes = encryptionKey.getBytes();
            System.arraycopy(originalBytes, 0, keyBytes, 0, Math.min(originalBytes.length, 32));

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES));
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar dado", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        try {
            Cipher cipher = Cipher.getInstance(AES);
            byte[] keyBytes = new byte[32];
            byte[] originalBytes = encryptionKey.getBytes();
            System.arraycopy(originalBytes, 0, keyBytes, 0, Math.min(originalBytes.length, 32));

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, AES));
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desencriptar dado", e);
        }
    }
}
