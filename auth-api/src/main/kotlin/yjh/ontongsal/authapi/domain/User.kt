package yjh.ontongsal.authapi.domain

import java.time.Instant

class User(
    val id: Long?,
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
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                deletedAt = null,
            )
        }
    }
}