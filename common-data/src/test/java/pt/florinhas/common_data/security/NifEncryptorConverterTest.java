package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NifEncryptorConverterTest {

    private CryptoUtils cryptoUtils;
    private NifEncryptorConverter converter;

    @BeforeEach
    void setUp() {

        cryptoUtils =
                mock(CryptoUtils.class);

        converter =
                new NifEncryptorConverter();

        converter.setCryptoUtils(
                cryptoUtils);
    }

    @Test
    void convertToDatabaseColumn_DeveCifrar() {

        when(cryptoUtils.encrypt("123456789"))
                .thenReturn("encrypted");

        String result =
                converter.convertToDatabaseColumn(
                        "123456789");

        assertEquals(
                "encrypted",
                result);
    }

    @Test
        void convertToDatabaseColumn_DeveIgnorarValorJaCifrado() {

        String result =
                converter.convertToDatabaseColumn(
                        "iv:data");

        assertEquals("iv:data", result);

        verify(cryptoUtils, never())
                .encrypt("iv:data");
        }

    @Test
    void convertToEntityAttribute_DeveDecifrar() {

        when(cryptoUtils.decrypt("iv:data"))
                .thenReturn("123456789");

        String result =
                converter.convertToEntityAttribute(
                        "iv:data");

        assertEquals(
                "123456789",
                result);
    }

    @Test
        void convertToEntityAttribute_DeveIgnorarTextoNaoCifrado() {

        String result =
                converter.convertToEntityAttribute(
                        "123456789");

        assertEquals("123456789", result);

        verify(cryptoUtils, never())
                .decrypt("123456789");
        }

    @Test
    void convertToDatabaseColumn_DeveRetornarNull() {

        assertEquals(
                null,
                converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_DeveRetornarNull() {

        assertEquals(
                null,
                converter.convertToEntityAttribute(null));
    }
    

}