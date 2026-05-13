package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CryptoUtilsTest {

    private CryptoUtils cryptoUtils;

    @BeforeEach
    void setUp() {

        cryptoUtils = new CryptoUtils();

        ReflectionTestUtils.setField(
                cryptoUtils,
                "encryptionKeyHex",
                "0123456789abcdef0123456789abcdef"
        );

        ReflectionTestUtils.setField(
                cryptoUtils,
                "blindIndexKey",
                "blind-key"
        );

        cryptoUtils.init();
    }

    @Test
    void encryptAndDecrypt_DeveFuncionar() {

        String original = "123456789";

        String encrypted =
                cryptoUtils.encrypt(original);

        String decrypted =
                cryptoUtils.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void generateBlindIndex_DeveGerarHash() {

        String hash =
                cryptoUtils.generateBlindIndex(
                        "123456789"
                );

        assertNotNull(hash);
        assertFalse(hash.isBlank());
    }

    @Test
    void encrypt_Null_DeveRetornarNull() {

        assertNull(
                cryptoUtils.encrypt(null)
        );
    }

    @Test
    void decrypt_Null_DeveRetornarNull() {

        assertNull(
                cryptoUtils.decrypt(null)
        );
    }
}