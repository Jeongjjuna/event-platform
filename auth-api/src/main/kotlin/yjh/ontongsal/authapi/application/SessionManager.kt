package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.JwtToken
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserSession
import yjh.ontongsal.authapi.infrastructure.UserSessionRepository
import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode

@Component
class SessionManager(
    private val userSessionRepository: UserSessionRepository,
) {
    fun append(user: User, jwtToken: JwtToken) {
        userSessionRepository.updateRefreshToken(user, jwtToken)
    }

    fun readSession(userId: Long): UserSession {
        return userSessionRepository.findByUserId(userId)
            ?: throw AppException.Unauthorized(ErrorCode.NOT_FOUND_SESSION)
    }
}