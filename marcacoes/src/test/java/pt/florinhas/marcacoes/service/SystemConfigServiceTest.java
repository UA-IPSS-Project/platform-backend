package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.SystemConfig;
import pt.florinhas.marcacoes.repository.SystemConfigRepository;

class SystemConfigServiceTest {

    private SystemConfigRepository systemConfigRepository;

    private SystemConfigService service;

    @BeforeEach
    void setup() {
        systemConfigRepository = mock(SystemConfigRepository.class);
        service = new SystemConfigService(systemConfigRepository);
    }

    @Test
    void getConfigValue_DeveRetornarValorConfigurado() {

        SystemConfig config =
                SystemConfig.builder()
                        .configKey("key")
                        .configValue("valor")
                        .build();

        when(systemConfigRepository
                .findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        String resultado =
                service.getConfigValue(
                        "key",
                        "default"
                );

        assertEquals(
                "valor",
                resultado
        );
    }

    @Test
    void getConfigValue_DeveRetornarDefault() {

        when(systemConfigRepository
                .findByConfigKey("key"))
                .thenReturn(Optional.empty());

        String resultado =
                service.getConfigValue(
                        "key",
                        "default"
                );

        assertEquals(
                "default",
                resultado
        );
    }

    @Test
    void getConfigValueAsInt_DeveRetornarNumero() {

        SystemConfig config =
                SystemConfig.builder()
                        .configKey("limite")
                        .configValue("25")
                        .build();

        when(systemConfigRepository
                .findByConfigKey("limite"))
                .thenReturn(Optional.of(config));

        int resultado =
                service.getConfigValueAsInt(
                        "limite",
                        5
                );

        assertEquals(
                25,
                resultado
        );
    }

    @Test
    void getConfigValueAsInt_DeveRetornarDefaultQuandoInvalido() {

        SystemConfig config =
                SystemConfig.builder()
                        .configKey("limite")
                        .configValue("abc")
                        .build();

        when(systemConfigRepository
                .findByConfigKey("limite"))
                .thenReturn(Optional.of(config));

        int resultado =
                service.getConfigValueAsInt(
                        "limite",
                        5
                );

        assertEquals(
                5,
                resultado
        );
    }

    @Test
    void setConfigValue_DeveAtualizarConfigExistente() {

        SystemConfig config =
                SystemConfig.builder()
                        .configKey("key")
                        .configValue("old")
                        .description("old desc")
                        .build();

        when(systemConfigRepository
                .findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        service.setConfigValue(
                "key",
                "novo",
                "nova desc"
        );

        assertEquals(
                "novo",
                config.getConfigValue()
        );

        assertEquals(
                "nova desc",
                config.getDescription()
        );

        verify(systemConfigRepository)
                .save(config);
    }

    @Test
    void setConfigValue_DeveCriarNovaConfig() {

        when(systemConfigRepository
                .findByConfigKey("nova"))
                .thenReturn(Optional.empty());

        service.setConfigValue(
                "nova",
                "123",
                "descricao"
        );

        verify(systemConfigRepository)
                .save(any(SystemConfig.class));
    }

    @Test
    void setConfigValue_NaoDeveAlterarDescricaoQuandoNull() {

        SystemConfig config =
                SystemConfig.builder()
                        .configKey("key")
                        .configValue("old")
                        .description("descricao antiga")
                        .build();

        when(systemConfigRepository
                .findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        service.setConfigValue(
                "key",
                "novo",
                null
        );

        assertEquals(
                "descricao antiga",
                config.getDescription()
        );

        verify(systemConfigRepository)
                .save(config);
    }
}