package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NifEncryptorConverterTest {

    private NifEncryptorConverter converter;

    private CryptoUtils cryptoUtils;

    @BeforeEach
    void setUp() {

        converter =
                new NifEncryptorConverter();

        cryptoUtils =
                mock(CryptoUtils.class);

        converter.setCryptoUtils(
                cryptoUtils
        );
    }

    @Test
    void convertToDatabaseColumn_DeveCifrar() {

        when(cryptoUtils.encrypt("123456789"))
                .thenReturn("encrypted");

        String result =
                converter.convertToDatabaseColumn(
                        "123456789"
                );

        assertEquals("encrypted", result);
    }

    @Test
    void convertToEntityAttribute_DeveDecifrar() {

        when(cryptoUtils.decrypt("iv:test"))
                .thenReturn("123456789");

        String result =
                converter.convertToEntityAttribute(
                        "iv:test"
                );

        assertEquals("123456789", result);
    }

    @Test
    void convertToDatabaseColumn_Null() {

        assertNull(
                converter.convertToDatabaseColumn(null)
        );
    }

    @Test
    void convertToEntityAttribute_Null() {

        assertNull(
                converter.convertToEntityAttribute(null)
        );
    }
}