package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.minio.MinioClient;

class MinioConfigTest {

    private MinioConfig config;

    @BeforeEach
    void setUp() throws Exception {

        config =
                new MinioConfig();

        setField("url", "http://localhost:9000");
        setField("accessKey", "admin");
        setField("secretKey", "password");
    }

    @Test
    void minioClient_DeveCriarCliente() {

        MinioClient client =
                config.minioClient();

        assertNotNull(client);
    }

    private void setField(
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                MinioConfig.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(config, value);
    }
}