package yjh.ontongsal.authapi.domain

import java.time.Instant

class UserSession(
    val userId: Long,
    val refreshToken: String,
    val updatedAt: Instant,
) {

    fun validateRefreshToken(refreshToken: String) {
        if (this.refreshToken != refreshToken) {
            throw IllegalArgumentException("Invalid refresh token")
        }
    }
}