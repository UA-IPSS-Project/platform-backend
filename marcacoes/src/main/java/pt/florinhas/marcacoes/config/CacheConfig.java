package pt.florinhas.marcacoes.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("assuntos",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build());
        manager.registerCustomCache("config-slots",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(10).build());
        manager.registerCustomCache("feriados",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(50).build());
        manager.registerCustomCache("terms-content",
            Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(5).build());
        return manager;
    }
}
