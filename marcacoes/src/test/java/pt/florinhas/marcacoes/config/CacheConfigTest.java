package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

class CacheConfigTest {

    private final CacheConfig config =
            new CacheConfig();

    @Test
    void cacheManager_DeveCriarBean() {

        CacheManager manager =
                config.cacheManager();

        assertNotNull(manager);

        assertInstanceOf(
                CaffeineCacheManager.class,
                manager);
    }

    @Test
    void cacheManager_DeveConterCaches() {

        CacheManager manager =
                config.cacheManager();

        assertNotNull(
                manager.getCache("assuntos"));

        assertNotNull(
                manager.getCache("agenda"));

        assertNotNull(
                manager.getCache("feriados"));
    }
}