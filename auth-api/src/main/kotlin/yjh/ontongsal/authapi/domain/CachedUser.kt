package yjh.ontongsal.authapi.domain

import kotlin.time.Instant

data class CachedUser(
    val id: Int,
    val email: String,
    val role: UserRole,
    val createdAt: Instant,
)