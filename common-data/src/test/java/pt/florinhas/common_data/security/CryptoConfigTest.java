package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class CryptoConfigTest {

    @Test
    void constructorEInit_DeveFuncionar() {

        CryptoUtils cryptoUtils =
                mock(CryptoUtils.class);

        CryptoConfig config =
                new CryptoConfig(cryptoUtils);

        assertNotNull(config);

        assertDoesNotThrow(config::init);
    }
}