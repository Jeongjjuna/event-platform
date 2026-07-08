package yjh.ontongsal.authapi.shared.security.jwt

class InvalidJwtException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)