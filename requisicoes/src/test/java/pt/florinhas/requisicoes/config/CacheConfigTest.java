package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

class CacheConfigTest {

    private final CacheConfig config =
            new CacheConfig();

    @Test
    void hibernate6Module_DeveCriarBean() {

        Hibernate6Module result =
                config.hibernate6Module();

        assertNotNull(result);
    }

    @Test
    void cacheManager_DeveCriarCaches() {

        CacheManager manager =
                config.cacheManager();

        assertNotNull(manager);

        assertNotNull(
                manager.getCache("materiais"));

        assertNotNull(
                manager.getCache("transportes"));

        assertNotNull(
                manager.getCache("tipos-manutencao"));

        assertNotNull(
                manager.getCache("manutencao-items"));
    }
}