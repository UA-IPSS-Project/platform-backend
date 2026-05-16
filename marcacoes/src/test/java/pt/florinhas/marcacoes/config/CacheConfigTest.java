package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
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

    @Test
    void cacheManager_DeveConterCacheConfigSlots() {

        CacheManager cacheManager =
                cacheConfig.cacheManager();

        CaffeineCache cache =
                (CaffeineCache) cacheManager.getCache(
                        "config-slots");

        assertNotNull(cache);
    }

    @Test
    void cacheManager_DeveConterCacheFeriados() {

        CacheManager cacheManager =
                cacheConfig.cacheManager();

        CaffeineCache cache =
                (CaffeineCache) cacheManager.getCache(
                        "feriados");

        assertNotNull(cache);
    }

    @Test
    void cacheManager_DeveConterCacheTermsContent() {

        CacheManager cacheManager =
                cacheConfig.cacheManager();

        CaffeineCache cache =
                (CaffeineCache) cacheManager.getCache(
                        "terms-content");

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