package yjh.ontongsal.authapi.shared.security.jwt

data class JwtUserInfo(
    val userId: Long,
    val email: String,
    val role: String,
)