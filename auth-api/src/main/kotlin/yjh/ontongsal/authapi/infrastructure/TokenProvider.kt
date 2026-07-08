package yjh.ontongsal.authapi.infrastructure

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.security.JwtProperties
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.sql.Date
import java.time.Instant

private val log = KotlinLogging.logger {}

@EnableConfigurationProperties(JwtProperties::class)
@Component
class TokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val privateKey: PrivateKey
    private val publicKey: PublicKey

    init {
        val keyFactory = KeyFactory.getInstance("RSA")

        // 개인키 로드 (PKCS#8 스펙)
        val privateKeyBytes = Decoders.BASE64.decode(jwtProperties.privateKey)
        this.privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

        // 공개키 로드 (X.509 스펙)
        val publicKeyBytes = Decoders.BASE64.decode(jwtProperties.publicKey)
        this.publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))
    }

    /**
     * MSA 분리환경이므로, 비대칭키(RS256) 서명 방식을 사용한다.
     */
    fun issueAccessToken(user: User): String {
        // 만료기한 설정
        val now = Instant.now()
        val exp = now.plus(jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.email)
            .claim("role", user.role)
            .signWith(privateKey)
            .compact()
    }

    /**
     * MSA 분리환경이므로, 비대칭키(RS256) 서명 방식을 사용한다.
     */
    fun issueRefreshToken(user: User): String {
        val now = Instant.now()
        val exp = now.plus(jwtProperties.refreshTokenExpiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.email)
            .claim("role", user.role)
            .signWith(privateKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
            return true
        } catch (e: JwtException) {
            log.info { "유효하지 않은 JWT 토큰입니다. (사유: ${e.message})" }
        } catch (e: IllegalArgumentException) {
            log.info { "JWT 토큰 문자열이 비어 있거나 잘못되었습니다." }
        }
        return false
    }
}
