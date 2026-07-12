package yjh.ontongsal.authapi.domain

import java.time.Instant

class UserRefreshToken(
    val userId: Long,
    val refreshToken: String,
    val updatedAt: Instant,
) {

    fun hasRefreshToken(refreshToken: String): Boolean {
        return this.refreshToken == refreshToken
    }
}