package yjh.ontongsal.authapi.infrastructure

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.stereotype.Repository
import yjh.ontongsal.authapi.application.UserCacheRepository
import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.caffeine.LocalCache

private val log = KotlinLogging.logger {}

@Repository
class UserCaffeineCacheRepository(
    @Qualifier("caffeineCacheManager")
    private val cacheManager: CacheManager
) : UserCacheRepository {

    private val cache: Cache =
        requireNotNull(cacheManager.getCache(LocalCache.USER_INFO.name))

    companion object {
        private const val KEY_PREFIX = "user:email:"
    }

    override fun get(email: String): CachedUser? {
        val key = "$KEY_PREFIX$email"

        return try {
            cache.get<CachedUser>(key)
        } catch (e: Exception) {
            log.error(e) { "[UserCache] Failed to get user data from Caffeine. Returning null. email=$email" }
            null
        }
    }

    override fun set(email: String, user: User): CachedUser {
        val key = "$KEY_PREFIX$email"

        val cachedUser = CachedUser(
            id = user.id!!,
            email = user.email,
            role = user.role,
            createdAt = user.createdAt
        )

        try {
            cache.put(key, cachedUser)
        } catch (e: Exception) {
            log.error(e) { "[UserCache] Failed to set user data to Caffeine. Ignored. email=$email" }
        }

        return cachedUser
    }
}