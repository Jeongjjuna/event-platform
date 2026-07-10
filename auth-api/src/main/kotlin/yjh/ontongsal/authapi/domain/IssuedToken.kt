package yjh.ontongsal.authapi.domain

data class IssuedToken(
    val accessToken: String,
    val refreshToken: String,
)
