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
}