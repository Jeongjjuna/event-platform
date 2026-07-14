package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.application.command.LoginCommand
import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.LoginResult
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

/**
 * 로그인 "비즈니스 로직"
 */
@Service
class LoginService(
    private val transaction: TransactionRunner,
    private val userReader: UserReader,
    private val userManager: UserManager,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager,
    private val userCache: UserCache,
) {

    fun login(loginCommand: LoginCommand): LoginResult {
        return transaction.run {
            val user = userReader.read(loginCommand.email)
            userManager.login(user, loginCommand.password)

            val jwtToken = tokenManager.issue(user)
            sessionManager.append(user, jwtToken)

            return@run LoginResult(user = user, jwtToken = jwtToken)
        }
    }

    fun getMyInfo(userId: Long, email: String): CachedUser {
        return userCache.read(email)
    }
}
