package yjh.ontongsal.authapi.shared.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import yjh.ontongsal.authapi.shared.cache.CacheType

@Configuration
@EnableCaching
class CaffeineConfig {

    @Bean("caffeineCacheManager")
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager();

        val caches = CacheType.entries.map {
            CaffeineCache(
                it.cacheName,
                Caffeine.newBuilder()
                    .expireAfterWrite(it.localTtl)
                    .maximumSize(it.localMaximumSize)
                    .build()
            )
        }

        return cacheManager.apply {
            setCaches(caches)
        }
    }
}