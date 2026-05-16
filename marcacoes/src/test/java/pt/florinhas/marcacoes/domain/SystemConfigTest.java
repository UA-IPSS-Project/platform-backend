package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SystemConfigTest {

    @Test
    void deveCriarSystemConfig() {

        SystemConfig config = SystemConfig.builder()
                .id(1L)
                .configKey("theme")
                .configValue("dark")
                .description("Tema")
                .build();

        assertEquals(1L, config.getId());
        assertEquals("theme", config.getConfigKey());
        assertEquals("dark", config.getConfigValue());
        assertEquals("Tema", config.getDescription());
    }

    @Test
    void onUpdateDeveDefinirUpdatedAt() {

        SystemConfig config = new SystemConfig();

        config.onUpdate();

        assertNotNull(config.getUpdatedAt());
    }
}