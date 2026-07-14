package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val sessionManager: SessionManager,
) {

    fun logout(userId: Long) {
        sessionManager.deleteSession(userId)
    }
}
