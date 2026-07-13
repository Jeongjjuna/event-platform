package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val tokenManager: TokenManager
) {

    fun logout(userId: Long) {
        tokenManager.deleteRefreshToken(userId)
    }
}
