package yjh.ontongsal.authapi.infrastructure

import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import yjh.ontongsal.authapi.application.UserCacheRepository
import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.cache.CacheType

/**
 * Spring Cache 어노테이션(@Cacheable, @CachePut) 기반 캐시 구현체
 */
@Repository
class UserAnnotationCacheRepository : UserCacheRepository {

    companion object {
        private const val CACHE_MANAGER = "caffeineCacheManager"
    }

    /**
     * 캐시 히트 시 본문이 실행되지 않고 캐시된 값이 바로 반환된다.
     * 캐시 미스 시 본문이 실행되어 null을 반환하며, unless 조건 때문에 null은 캐싱되지 않는다.
     */
    @Cacheable(
        cacheNames = [CacheType.Name.USER_INFO],
        cacheManager = CACHE_MANAGER,
        key = "#email",
        unless = "#result == null"
    )
    override fun get(email: String): CachedUser? = null

    /**
     * 본문이 항상 실행되고, 반환값이 캐시에 저장된다.
     */
    @CachePut(cacheNames = [CacheType.Name.USER_INFO], cacheManager = CACHE_MANAGER, key = "#email")
    override fun set(email: String, user: User): CachedUser =
        CachedUser(
            id = user.id!!,
            email = user.email,
            role = user.role,
            createdAt = user.createdAt
        )
}
