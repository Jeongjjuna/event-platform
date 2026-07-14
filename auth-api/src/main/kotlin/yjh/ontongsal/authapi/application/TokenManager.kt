package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.JwtToken
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider
import yjh.ontongsal.authapi.shared.security.jwt.JwtUserInfo
import yjh.ontongsal.authapi.shared.security.jwt.TokenType

@Component
class TokenManager(
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun issue(user: User): JwtToken {
        val accessToken = jwtTokenProvider.issueAccessToken(user)
        val refreshToken = jwtTokenProvider.issueRefreshToken(user)

        return JwtToken(
            grantType = "Bearer",
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun parseToken(token: String, tokenType: TokenType): JwtUserInfo {
        if (!jwtTokenProvider.validateToken(token, tokenType)) {
            throw AppException.Unauthorized(ErrorCode.INVALID_TOKEN_TYPE)
        }
        return jwtTokenProvider.getUserInfo(token)
    }
}