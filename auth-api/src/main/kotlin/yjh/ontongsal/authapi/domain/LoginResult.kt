package yjh.ontongsal.authapi.domain

data class LoginResult(
    val user: User,
    val jwtToken: JwtToken,
)