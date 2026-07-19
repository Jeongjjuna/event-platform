package yjh.ontongsal.authapi.domain

data class RefreshResult(
    val user: User,
    val jwtToken: JwtToken,
)
