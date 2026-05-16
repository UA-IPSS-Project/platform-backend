package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import io.minio.MinioClient;

class MinioConfigTest {

    @Test
    @DisplayName("Deve criar MinioClient")
    void minioClient_DeveCriarClient() {

        MinioConfig config = new MinioConfig();

        ReflectionTestUtils.setField(
                config,
                "url",
                "http://localhost:9000"
        );

        ReflectionTestUtils.setField(
                config,
                "accessKey",
                "admin"
        );

        ReflectionTestUtils.setField(
                config,
                "secretKey",
                "password"
        );

        MinioClient client = config.minioClient();

        assertNotNull(client);
    }
}