package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.IssuedToken
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.infrastructure.RefreshTokenRepository
import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider
import yjh.ontongsal.authapi.shared.security.jwt.JwtUserInfo

@Component
class TokenManager(
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun issue(user: User): IssuedToken {
        val accessToken = jwtTokenProvider.issueAccessToken(user)
        val refreshToken = jwtTokenProvider.issueRefreshToken(user)
        return IssuedToken(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun validateRefreshToken(refreshToken: String): JwtUserInfo {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw AppException.Unauthorized(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val jwtUserInfo: JwtUserInfo = jwtTokenProvider.getUserInfo(refreshToken)
        val userRefreshToken = refreshTokenRepository.findByUserId(jwtUserInfo.userId)
            ?: throw AppException.Unauthorized(ErrorCode.INVALID_REFRESH_TOKEN)

        if (!userRefreshToken.hasRefreshToken(refreshToken)) {
            throw AppException.Unauthorized(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        return jwtUserInfo
    }

    fun refreshToken(userId: Long, refreshToken: String) {
        refreshTokenRepository.updateUserRefreshToken(userId, refreshToken)
    }

    fun reissueToken(jwtUserInfo: JwtUserInfo): IssuedToken {
        val accessToken = jwtTokenProvider.issueAccessToken(jwtUserInfo)
        val refreshToken = jwtTokenProvider.issueRefreshToken(jwtUserInfo)

        refreshTokenRepository.updateUserRefreshToken(jwtUserInfo.userId, refreshToken)

        return IssuedToken(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun deleteRefreshToken(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
    }
}