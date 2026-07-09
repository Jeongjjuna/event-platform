package yjh.ontongsal.authapi.shared.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CaffeineConfig {

    @Bean("caffeineCacheManager")
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager();

        val caches = LocalCache.entries.map {
            CaffeineCache(
                it.namespace,
                Caffeine.newBuilder()
                    .expireAfterWrite(it.ttl)
                    .maximumSize(it.maximumSize)
                    .build()
            )
        }

        return cacheManager.apply {
            setCaches(caches)
        }
    }
}