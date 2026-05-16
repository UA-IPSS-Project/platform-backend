package pt.florinhas.common_data.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.common_data.exception.CryptoException;

@Component
public class CryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.security.encryption.key}")
    private String encryptionKeyHex;

    @Value("${app.security.blindindex.key}")
    private String blindIndexKey;

    private byte[] encryptionKeyBytes;

    @PostConstruct
    public void init() {
        if (encryptionKeyHex == null || encryptionKeyHex.isBlank()) {
            throw new IllegalStateException("app.security.encryption.key não está configurado");
        }
        if (blindIndexKey == null || blindIndexKey.isBlank()) {
            throw new IllegalStateException("app.security.blindindex.key não está configurado");
        }

        encryptionKeyBytes = hexToBytes(encryptionKeyHex.trim());
        int len = encryptionKeyBytes.length;
        if (len != 16 && len != 24 && len != 32) {
            throw new IllegalStateException(
                "app.security.encryption.key deve ser hex de 32, 48 ou 64 chars (16/24/32 bytes AES). Tamanho atual: " + len + " bytes");
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKeyBytes, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CryptoException("Erro ao cifrar dados", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        try {
            String[] parts = cipherText.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato inválido do texto cifrado. Esperado 'iv:ciphertext'.");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKeyBytes, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("Erro ao decifrar dados", e);
        }
    }

    public String generateBlindIndex(String plainText) {
        if (plainText == null) return null;
        String normalized = NifValidator.normalize(plainText);
        if (normalized == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((normalized + blindIndexKey).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new CryptoException("Erro ao gerar blind index", e);
        }
    }

    private static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalStateException("app.security.encryption.key hex inválido (comprimento ímpar)");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalStateException("app.security.encryption.key contém caracteres não-hex");
            }
            bytes[i] = (byte) ((hi << 4) | lo);
        }
        return bytes;
    }
}
