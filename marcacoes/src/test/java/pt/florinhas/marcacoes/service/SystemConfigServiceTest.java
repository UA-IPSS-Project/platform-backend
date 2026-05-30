package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.SystemConfig;
import pt.florinhas.marcacoes.repository.SystemConfigRepository;

class SystemConfigServiceTest {

    private SystemConfigRepository repository;
    private SystemConfigService service;

    @BeforeEach
    void setUp() throws Exception {
        repository = mock(SystemConfigRepository.class);
        service = new SystemConfigService();
        setField("systemConfigRepository", repository);
    }

    @Test
    void getConfigValue_DeveRetornarValor() {

        SystemConfig config = new SystemConfig();
        config.setConfigValue("10");

        when(repository.findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        String result = service.getConfigValue("key", "default");

        assertEquals("10", result);
    }

    @Test
    void getConfigValue_DeveRetornarDefault() {

        when(repository.findByConfigKey("key"))
                .thenReturn(Optional.empty());

        String result = service.getConfigValue("key", "default");

        assertEquals("default", result);
    }

    @Test
    void getConfigValueAsInt_DeveRetornarInt() {

        SystemConfig config = new SystemConfig();
        config.setConfigValue("15");

        when(repository.findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        int result = service.getConfigValueAsInt("key", 5);

        assertEquals(15, result);
    }

    @Test
    void getConfigValueAsInt_DeveRetornarDefaultQuandoInvalido() {

        SystemConfig config = new SystemConfig();
        config.setConfigValue("abc");

        when(repository.findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        int result = service.getConfigValueAsInt("key", 5);

        assertEquals(5, result);
    }

    @Test
    void setConfigValue_DeveAtualizarExistente() {

        SystemConfig config = new SystemConfig();

        when(repository.findByConfigKey("key"))
                .thenReturn(Optional.of(config));

        service.setConfigValue("key", "value", "desc");

        assertEquals("value", config.getConfigValue());
        assertEquals("desc", config.getDescription());
        verify(repository).save(config);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = SystemConfigService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}