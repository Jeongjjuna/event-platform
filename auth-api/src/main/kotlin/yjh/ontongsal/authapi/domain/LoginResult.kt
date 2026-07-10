package yjh.ontongsal.authapi.domain

data class LoginResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)
