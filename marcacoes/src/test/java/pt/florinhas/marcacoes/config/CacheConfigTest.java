package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

class CacheConfigTest {

    private final CacheConfig cacheConfig =
            new CacheConfig();

    @Test
    void cacheManager_DeveCriarCacheManager() {

        CacheManager cacheManager =
                cacheConfig.cacheManager();

        assertNotNull(cacheManager);
    }

    @Test
    void cacheManager_DeveConterCacheAssuntos() {

        CacheManager cacheManager =
                cacheConfig.cacheManager();

        CaffeineCache cache =
                (CaffeineCache) cacheManager.getCache(
                        "assuntos");

        assertNotNull(cache);

        assertTrue(
                cache.getNativeCache()
                        .policy()
                        .eviction()
                        .isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"config-slots", "feriados", "terms-content"})
    void cacheManager_DeveConterCaches(String cacheName) {
        CacheManager cacheManager = cacheConfig.cacheManager();
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        assertNotNull(cache);
    }

    @Test
    void caches_DevePermitirPutEGet() {

        CacheManager cacheManager =
                cacheConfig.cacheManager();

        CaffeineCache cache =
                (CaffeineCache) cacheManager.getCache(
                        "assuntos");

        assertNotNull(cache);

        cache.put(
                "key",
                "value");

        String value =
                cache.get(
                        "key",
                        String.class);

        assertNotNull(value);

        org.junit.jupiter.api.Assertions.assertEquals(
                "value",
                value);
    }
}