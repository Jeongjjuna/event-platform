package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.domain.IssuedToken

@Service
class RefreshService(
    private val tokenManager: TokenManager
) {

    fun refreshToken(refreshToken: String): IssuedToken {
        val jwtUserInfo = tokenManager.validateRefreshToken(refreshToken)
        return tokenManager.reissueToken(jwtUserInfo);
    }
}