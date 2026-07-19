package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.domain.RefreshResult
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner
import yjh.ontongsal.authapi.shared.security.jwt.TokenType

@Service
class RefreshService(
    private val transaction: TransactionRunner,
    private val userReader: UserReader,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager
) {

    fun refreshToken(refreshToken: String): RefreshResult {
        return transaction.run {
            val jwtUserInfo = tokenManager.parseToken(refreshToken, TokenType.REFRESH)

            val userSession = sessionManager.readSession(jwtUserInfo.userId)
            userSession.validateRefreshToken(refreshToken)

            val user = userReader.read(jwtUserInfo.email)
            val newJwtToken = tokenManager.issue(user);

            sessionManager.append(user, newJwtToken)
            return@run RefreshResult(user, newJwtToken)
        }

    }
}