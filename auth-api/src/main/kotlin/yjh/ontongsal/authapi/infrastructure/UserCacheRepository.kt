package yjh.ontongsal.authapi.infrastructure

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper
import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserRole
import java.time.Duration
import kotlin.time.Instant

private val log = KotlinLogging.logger {}

/**
 * Redis에 JSON 형태로 저장하기 위한 캐시 전용 DTO
 */
data class UserCacheDto(
    val id: Int,
    val email: String,
    val role: String,
    val createdAt: Instant,
)

@Repository
class UserCacheRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val KEY_PREFIX = "user:email:"
        private val TTL = Duration.ofMinutes(30)
    }

    fun get(email: String): CachedUser? {
        val key = "$KEY_PREFIX$email"

        return try {
            val jsonString = redisTemplate.opsForValue().get(key) ?: return null

            val dto = objectMapper.readValue(jsonString, UserCacheDto::class.java)
            CachedUser(dto.id, dto.email, UserRole.valueOf(dto.role), dto.createdAt)
        } catch (e: Exception) {
            log.error(e) { "[UserCache] Failed to get user data from Redis. Returning null. email=$email" }
            null
        }
    }

    fun set(email: String, user: User): CachedUser {
        val key = "$KEY_PREFIX$email"

        try {
            val dto = UserCacheDto(id = user.id!!, email = user.email, role = user.role.name, createdAt = user.createdAt)

            val jsonString = objectMapper.writeValueAsString(dto)
            redisTemplate.opsForValue().set(key, jsonString, TTL)
        } catch (e: Exception) {
            log.error(e) { "[UserCache] Failed to set user data to Redis. Ignored. email=$email" }
        }

        return CachedUser(user.id!!, user.email, user.role, user.createdAt)
    }
}