package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class CryptoConfigTest {

    @Test
    void init_DeveConfigurarCryptoUtils() {

        CryptoUtils cryptoUtils =
                new CryptoUtils();

        CryptoConfig config =
                new CryptoConfig(
                        cryptoUtils);

        assertDoesNotThrow(
                config::init);
    }
}