package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CryptoConfigTest {

    @Test
    void init_DeveExecutarSemErro() {

        CryptoUtils cryptoUtils =
                org.mockito.Mockito.mock(
                        CryptoUtils.class);

        CryptoConfig config =
                new CryptoConfig(
                        cryptoUtils);

        config.init();

        assertNotNull(config);
    }
}