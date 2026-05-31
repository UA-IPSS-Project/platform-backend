package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SystemConfigTest {

    @Test
    void onUpdate_DeveDefinirUpdatedAt() {

        SystemConfig config = new SystemConfig();

        config.onUpdate();

        assertNotNull(config.getUpdatedAt());
    }
}