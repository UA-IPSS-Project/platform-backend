package pt.florinhas.common_data;

import org.springframework.test.util.ReflectionTestUtils;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.security.NifEncryptorConverter;

public class TestCryptoConfig {

    public static void initCrypto() {

        CryptoUtils cryptoUtils =
                new CryptoUtils();

        ReflectionTestUtils.setField(
                cryptoUtils,
                "encryptionKeyHex",
                "0123456789abcdef0123456789abcdef"
        );

        ReflectionTestUtils.setField(
                cryptoUtils,
                "blindIndexKey",
                "test-key"
        );

        cryptoUtils.init();

        /*
         * Utilizador static crypto
         */
        Utilizador.setCryptoUtils(
                cryptoUtils
        );

        /*
         * NifEncryptorConverter static crypto
         */
        ReflectionTestUtils.setField(
                NifEncryptorConverter.class,
                "cryptoUtils",
                cryptoUtils
        );
    }
}