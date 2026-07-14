package yjh.ontongsal.authapi.domain

import java.time.Instant

class User(
    val id: Long,
    val email: String,
    var password: String,
    val role: UserRole,
    var lastLoginAt: Instant?,
    val createdAt: Instant,
    var updatedAt: Instant,
    var deletedAt: Instant?,
) {
    fun changePassword(hashedPassword: String) {
        this.password = hashedPassword
        this.updatedAt = Instant.now()
    }

    fun withdraw() {
        val now = Instant.now()
        this.updatedAt = now
        this.deletedAt = now
    }

    fun login(now: Instant) {
        if (this.deletedAt != null) {
            throw IllegalStateException("User is already withdrawn")
        }
        this.lastLoginAt = now
        this.updatedAt = now
    }
}