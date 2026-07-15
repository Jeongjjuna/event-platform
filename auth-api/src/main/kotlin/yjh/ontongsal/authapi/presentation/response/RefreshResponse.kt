package yjh.ontongsal.authapi.presentation.response

import yjh.ontongsal.authapi.domain.JwtToken

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(jwtToken: JwtToken): RefreshResponse {
            return RefreshResponse(jwtToken.accessToken, jwtToken.refreshToken)
        }
    }
}