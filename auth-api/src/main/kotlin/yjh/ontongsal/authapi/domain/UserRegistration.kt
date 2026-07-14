package yjh.ontongsal.authapi.domain

import java.time.Instant

class UserRegistration(
    val email: String,
    val password: String,
    val role: UserRole,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
) {

    companion object {
        fun of(
            email: String,
            password: String,
            role: UserRole,
            now: Instant,
        ) = UserRegistration(
            email = email,
            password = password,
            role = role,
            lastLoginAt = null,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        )
    }
}