package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.LoginInfo
import yjh.ontongsal.authapi.domain.LoginResult
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

/** 로그인 "비즈니스 로직" */
@Service
class LoginService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    private val userCache: UserCache,
) {

    fun login(loginInfo: LoginInfo): LoginResult {
        val user = transaction.run {
            userManager.authenticate(loginInfo)
        }

        val (accessToken, refreshToken) = tokenManager.issue(user)

        return LoginResult(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun getMyInfo(userId: Long, email: String): CachedUser {
        return userCache.read(email)
    }
}
