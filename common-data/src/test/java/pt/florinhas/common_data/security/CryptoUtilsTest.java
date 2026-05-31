package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CryptoUtilsTest {

    private CryptoUtils cryptoUtils;

    @BeforeEach
    void setUp() throws Exception {

        cryptoUtils = new CryptoUtils();

        setField(
                "encryptionKeyHex",
                "00112233445566778899AABBCCDDEEFF");

        setField(
                "blindIndexKey",
                "blind-key");

        cryptoUtils.init();
    }

    @Test
    void encrypt_DeveCifrarTexto() {

        String encrypted =
                cryptoUtils.encrypt(
                        "123456789");

        assertNotNull(encrypted);
        assertNotEquals(
                "123456789",
                encrypted);
    }

    @Test
    void decrypt_DeveDecifrarTexto() {

        String encrypted =
                cryptoUtils.encrypt(
                        "123456789");

        String decrypted =
                cryptoUtils.decrypt(encrypted);

        assertEquals(
                "123456789",
                decrypted);
    }

    @Test
    void generateBlindIndex_DeveGerarHash() {

        String hash =
                cryptoUtils.generateBlindIndex(
                        "123456789");

        assertNotNull(hash);
    }

    @Test
    void encrypt_DeveRetornarNull() {

        assertEquals(
                null,
                cryptoUtils.encrypt(null));
    }

    @Test
    void decrypt_DeveRetornarNull() {

        assertEquals(
                null,
                cryptoUtils.decrypt(null));
    }

    @Test
    void generateBlindIndex_DeveRetornarNull() {

        assertEquals(
                null,
                cryptoUtils.generateBlindIndex(null));
    }

    @Test
    void decrypt_DeveFalharComFormatoInvalido() {

        assertThrows(
                RuntimeException.class,
                () -> cryptoUtils.decrypt("invalido"));
    }

    @Test
    void init_DeveFalharSemEncryptionKey() throws Exception {

        CryptoUtils utils =
                new CryptoUtils();

        setField(
                utils,
                "encryptionKeyHex",
                null);

        setField(
                utils,
                "blindIndexKey",
                "blind");

        assertThrows(
                IllegalStateException.class,
                utils::init);
    }

    @Test
    void init_DeveFalharSemBlindKey() throws Exception {

        CryptoUtils utils =
                new CryptoUtils();

        setField(
                utils,
                "encryptionKeyHex",
                "00112233445566778899AABBCCDDEEFF");

        setField(
                utils,
                "blindIndexKey",
                null);

        assertThrows(
                IllegalStateException.class,
                utils::init);
    }

    @Test
    void init_DeveAceitarChaveValida() {

        assertDoesNotThrow(
                () -> cryptoUtils.init());
    }

    private void setField(
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                CryptoUtils.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(
                cryptoUtils,
                value);
    }

    private void setField(
            CryptoUtils target,
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                CryptoUtils.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(
                target,
                value);
    }

    @Test
        void generateBlindIndex_DeveSerDeterministico() {

        String hash1 =
                cryptoUtils.generateBlindIndex(
                        "123456789");

        String hash2 =
                cryptoUtils.generateBlindIndex(
                        "123456789");

        assertEquals(hash1, hash2);
        }

        @Test
        void generateBlindIndex_DeveGerarHashesDiferentes() {

        String hash1 =
                cryptoUtils.generateBlindIndex(
                        "123456789");

        String hash2 =
                cryptoUtils.generateBlindIndex(
                        "987654321");

        assertNotEquals(hash1, hash2);
        }

        @Test
        void generateBlindIndex_DeveNormalizarNif() {

        String hash1 =
                cryptoUtils.generateBlindIndex(
                        "123 456 789");

        String hash2 =
                cryptoUtils.generateBlindIndex(
                        "123456789");

        assertEquals(hash1, hash2);
        }

        @Test
        void encryptDecrypt_DeveSuportarCaracteresEspeciais() {

        String original =
                "Olá ç ã € 漢字 :)";

        String encrypted =
                cryptoUtils.encrypt(original);

        String decrypted =
                cryptoUtils.decrypt(encrypted);

        assertEquals(original, decrypted);
        }

        @Test
        void encryptDecrypt_DeveSuportarStringVazia() {

        String encrypted =
                cryptoUtils.encrypt("");

        String decrypted =
                cryptoUtils.decrypt(encrypted);

        assertEquals("", decrypted);
        }

        @Test
        void generateBlindIndex_DeveRetornarNullParaStringVazia() {

        assertEquals(null,
                cryptoUtils.generateBlindIndex(""));
        }
}