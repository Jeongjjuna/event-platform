package yjh.ontongsal.authapi.application

import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.User
import java.time.Instant

/**
 * Redis에 JSON 형태로 저장하기 위한 캐시 전용 DTO
 */
data class UserCacheDto(
    val id: Long,
    val email: String,
    val role: String,
    val createdAt: Instant,
)

interface UserCacheRepository {
    fun get(email: String): CachedUser?
    fun set(email: String, user: User): CachedUser
}