package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.TestUtils;

class CryptoUtilsTest {

    private CryptoUtils cryptoUtils;

    @BeforeEach
    void setUp() {

        cryptoUtils =
                new CryptoUtils();

        TestUtils.setField(
                cryptoUtils,
                "encryptionKeyHex",
                "0123456789ABCDEF0123456789ABCDEF");

        TestUtils.setField(
                cryptoUtils,
                "blindIndexKey",
                "blind-key");

        cryptoUtils.init();
    }

    @Test
    void encryptDecrypt_DeveFuncionar() {

        String encrypted =
                cryptoUtils.encrypt(
                        "123456789");

        assertNotEquals(
                "123456789",
                encrypted);

        String decrypted =
                cryptoUtils.decrypt(
                        encrypted);

        assertEquals(
                "123456789",
                decrypted);
    }

    @Test
    void encrypt_DeveAceitarNull() {

        assertEquals(
                null,
                cryptoUtils.encrypt(null));
    }

    @Test
    void decrypt_DeveAceitarNull() {

        assertEquals(
                null,
                cryptoUtils.decrypt(null));
    }

    @Test
    void generateBlindIndex_DeveGerarHash() {

        String hash =
                cryptoUtils.generateBlindIndex(
                        "123456789");

        assertNotEquals(
                null,
                hash);
    }

    @Test
    void decrypt_DeveLancarExcecaoFormatoInvalido() {

        assertThrows(
                RuntimeException.class,
                () -> cryptoUtils.decrypt(
                        "invalido"));
    }

    @Test
    void init_DeveLancarExcecaoQuandoKeyInvalida() {

        CryptoUtils utils =
                new CryptoUtils();

        TestUtils.setField(
                utils,
                "encryptionKeyHex",
                "123");

        TestUtils.setField(
                utils,
                "blindIndexKey",
                "blind");

        assertThrows(
                IllegalStateException.class,
                utils::init);
    }

    @Test
    void init_DeveLancarExcecaoQuandoBlindKeyNull() {

        CryptoUtils utils =
                new CryptoUtils();

        TestUtils.setField(
                utils,
                "encryptionKeyHex",
                "0123456789ABCDEF0123456789ABCDEF");

        TestUtils.setField(
                utils,
                "blindIndexKey",
                null);

        assertThrows(
                IllegalStateException.class,
                utils::init);
    }

    @Test
    void init_DeveExecutarSemErro() {

        assertDoesNotThrow(
                () -> cryptoUtils.init());
    }
}