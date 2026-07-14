package yjh.ontongsal.authapi.domain

import yjh.ontongsal.authapi.shared.response.DomainException
import java.time.Instant

class UserSession(
    val userId: Long,
    val refreshToken: String,
    val updatedAt: Instant,
) {

    fun validateRefreshToken(refreshToken: String) {
        if (this.refreshToken != refreshToken) {
            throw DomainException.Unauthorized(AuthErrorCode.SESSION_TOKEN_MISMATCH)
        }
    }
}