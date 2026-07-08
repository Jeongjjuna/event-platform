package yjh.ontongsal.authapi.infrastructure

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.security.JwtProperties
import java.sql.Date
import java.time.Instant


@EnableConfigurationProperties(JwtProperties::class)
@Component
class TokenProvider(
    private val jwtProperties: JwtProperties,
) {

    /**
     * 대칭키 서명방식을 사용한다.
     */
    fun issueAccessToken(user: User): String {
        val now = Instant.now()
        val exp = now.plus(jwtProperties.accessTokenExpiration)
        val secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secretKey))

        return Jwts.builder()
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.email)
            .claim("role", user.role)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 대칭키 서명방식을 사용한다.
     */
    fun issueRefreshToken(user: User): String {
        val now = Instant.now()
        val exp = now.plus(jwtProperties.refreshTokenExpiration)
        val secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secretKey))

        return Jwts.builder()
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.email)
            .claim("role", user.role)
            .signWith(secretKey)
            .compact()
    }
}
