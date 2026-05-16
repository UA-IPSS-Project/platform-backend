package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.TestUtils;

class NifEncryptorConverterTest {

    private NifEncryptorConverter converter;

    @BeforeEach
    void setUp() {

        CryptoUtils cryptoUtils =
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

        converter =
                new NifEncryptorConverter();

        converter.setCryptoUtils(
                cryptoUtils);
    }

    @Test
    void convertToDatabaseColumn_DeveCifrar() {

        String encrypted =
                converter.convertToDatabaseColumn(
                        "123456789");

        assertNotEquals(
                "123456789",
                encrypted);
    }

    @Test
    void convertToEntityAttribute_DeveDecifrar() {

        String encrypted =
                converter.convertToDatabaseColumn(
                        "123456789");

        String decrypted =
                converter.convertToEntityAttribute(
                        encrypted);

        assertEquals(
                "123456789",
                decrypted);
    }

    @Test
    void convertToDatabaseColumn_DeveAceitarNull() {

        assertEquals(
                null,
                converter.convertToDatabaseColumn(
                        null));
    }

    @Test
    void convertToEntityAttribute_DeveAceitarNull() {

        assertEquals(
                null,
                converter.convertToEntityAttribute(
                        null));
    }

    @Test
    void convertToDatabaseColumn_DeveManterValorJaCifrado() {

        String value =
                "abc:def";

        assertEquals(
                value,
                converter.convertToDatabaseColumn(
                        value));
    }

    @Test
    void convertToEntityAttribute_DeveManterValorNaoCifrado() {

        String value =
                "123456789";

        assertEquals(
                value,
                converter.convertToEntityAttribute(
                        value));
    }

    @Test
    void converter_DeveLancarExcecaoSemCryptoUtils() {

        TestUtils.setField(
                NifEncryptorConverter.class,
                "cryptoUtils",
                null);

        NifEncryptorConverter converter =
                new NifEncryptorConverter();

        assertThrows(
                IllegalStateException.class,
                () -> converter.convertToDatabaseColumn(
                        "123456789"));
    }
}