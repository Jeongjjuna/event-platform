package yjh.ontongsal.authapi.application

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.AuthErrorCode
import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.infrastructure.UserRepository
import yjh.ontongsal.authapi.shared.response.AppException

/**
 * 구현 계층으로, 캐시 전략등을 적용한다.
 */
@Component
class UserCache(
    private val userRepository: UserRepository,

    @Qualifier("userCaffeineCacheRepository")
    private val userCacheRepository: UserCacheRepository,
) {

    /**
     * Cache Aside 전략 (find)
     * 조회 결과가 없으면 null을 반환한다.
     */
    fun find(email: String): CachedUser? {
        // 1. 캐시 조회 (Cache Hit)
        val cachedUser = userCacheRepository.get(email)
        if (cachedUser != null) {
            return cachedUser
        }

        // 2. DB 조회 (Cache Miss)
        val user = userRepository.findByEmail(email) ?: return null

        // 3. 캐시 저장 및 반환
        return userCacheRepository.set(email, user)
    }

    /**
     * Cache Aside 전략 (read)
     * 조회 결과가 없으면 예외를 발생시킨다.
     */
    fun read(email: String): CachedUser {
        // 1. 캐시 조회 (Cache Hit)
        val cachedUser = userCacheRepository.get(email)
        if (cachedUser != null) {
            return cachedUser
        }

        // 2. DB 조회 (Cache Miss)
        val user = userRepository.findByEmail(email)
            ?: throw AppException.NotFound(AuthErrorCode.USER_NOT_FOUND)

        // 3. 캐시 저장 및 반환
        return userCacheRepository.set(email, user)
    }

    fun evict(email: String) {
        userCacheRepository.evict(email)
    }
}