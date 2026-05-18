package pt.florinhas.requisicoes.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
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
    Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("materiais",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(500).build());
        manager.registerCustomCache("transportes",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(200).build());
        manager.registerCustomCache("tipos-manutencao",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(100).build());
        manager.registerCustomCache("manutencao-items",
            Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(500).build());
        return manager;
    }
}
