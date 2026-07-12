package yjh.ontongsal.authapi.domain

import java.time.Instant

data class CachedUser(
    val id: Long,
    val email: String,
    val role: UserRole,
    val createdAt: Instant,
)