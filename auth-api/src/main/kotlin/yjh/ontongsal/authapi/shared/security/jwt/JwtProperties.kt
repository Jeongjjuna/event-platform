package yjh.ontongsal.authapi.shared.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val issuer: String,
    val privateKey: String,
    val publicKey: String,
    val accessTokenExpiration: Duration,
    val refreshTokenExpiration: Duration
)