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
import yjh.ontongsal.authapi.shared.cache.CacheType

private val log = KotlinLogging.logger {}

@Repository
class UserCaffeineCacheRepository(
    @Qualifier("caffeineCacheManager")
    private val cacheManager: CacheManager
) : UserCacheRepository {

    private val cacheType: CacheType = CacheType.from(CacheType.Name.USER_INFO)
    private val cache: Cache = requireNotNull(cacheManager.getCache(cacheType.cacheName))

    override fun get(email: String): CachedUser? {
        // 캐시 이름(user_info)으로 이미 격리된 캐시이므로 email을 그대로 키로 사용한다.
        return try {
            cache.get<CachedUser>(email)
        } catch (e: Exception) {
            log.error(e) { "[UserCache] Failed to get user data from Caffeine. Returning null. email=$email" }
            null
        }
    }

    override fun set(email: String, user: User): CachedUser {
        val cachedUser = CachedUser(
            id = user.id!!,
            email = user.email,
            role = user.role,
            createdAt = user.createdAt
        )

        try {
            cache.put(email, cachedUser)
        } catch (e: Exception) {
            log.error(e) { "[UserCache] Failed to set user data to Caffeine. Ignored. email=$email" }
        }

        return cachedUser
    }
}