package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.IssuedToken
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider

@Component
class TokenManager(
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun issue(user: User): IssuedToken {
        val accessToken = jwtTokenProvider.issueAccessToken(user)
        val refreshToken = jwtTokenProvider.issueRefreshToken(user)
        return IssuedToken(accessToken = accessToken, refreshToken = refreshToken)
    }
}