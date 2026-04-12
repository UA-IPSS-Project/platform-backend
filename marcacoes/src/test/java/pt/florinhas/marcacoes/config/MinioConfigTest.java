package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.minio.MinioClient;

class MinioConfigTest {

    private MinioConfig minioConfig;

    @BeforeEach
    void setUp() throws Exception {
        minioConfig = new MinioConfig();

        setField(minioConfig, "url", "http://localhost:9000");
        setField(minioConfig, "accessKey", "minioadmin");
        setField(minioConfig, "secretKey", "minioadmin");
    }

    @Test
    void minioClient_DeveCriarClienteComSucesso() {
        MinioClient client = minioConfig.minioClient();

        assertNotNull(client);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}