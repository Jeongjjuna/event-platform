package yjh.ontongsal.authapi.domain

import kotlin.time.Clock
import kotlin.time.Instant

class User(
    val id: Int?,
    val email: String,
    val password: String,
    val role: UserRole,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
) {

    companion object {
        fun signUp(email: String, password: String): User {
            return User(
                id = null,
                email = email,
                password = password,
                role = UserRole.USER,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                deletedAt = null,
            )
        }
    }
}