package yjh.ontongsal.authapi.shared.security.jwt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.shared.security.SecurityUserDetails
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.sql.Date
import java.time.Instant
import java.util.*

private val log = KotlinLogging.logger {}

@EnableConfigurationProperties(JwtProperties::class)
@Component
class JwtTokenProvider(
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
            .id(UUID.randomUUID().toString())
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.email)
            .claim("role", user.role)
            .claim("token_type", TokenType.ACCESS.name)
            .signWith(privateKey)
            .compact()
    }

    fun issueAccessToken(jwtUserInfo: JwtUserInfo): String {
        // 만료기한 설정
        val now = Instant.now()
        val exp = now.plus(jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(jwtUserInfo.userId.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", jwtUserInfo.email)
            .claim("role", jwtUserInfo.role)
            .claim("token_type", TokenType.ACCESS.name)
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
            .id(UUID.randomUUID().toString())
            .subject(user.id.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.email)
            .claim("role", user.role)
            .claim("token_type", TokenType.REFRESH.name)
            .signWith(privateKey)
            .compact()
    }

    fun issueRefreshToken(jwtUserInfo: JwtUserInfo): String {
        val now = Instant.now()
        val exp = now.plus(jwtProperties.refreshTokenExpiration)

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(jwtUserInfo.userId.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", jwtUserInfo.email)
            .claim("role", jwtUserInfo.role)
            .claim("token_type", TokenType.REFRESH.name)
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

    /**
     * 서명/만료 검증에 더해 token_type 클레임이 기대한 타입과 일치하는지 검증한다.
     */
    fun validateToken(token: String, expectedType: TokenType): Boolean {
        if (!validateToken(token)) {
            return false
        }

        val tokenType = getTokenType(getClaims(token))
        if (tokenType.isNotEqualTo(expectedType)) {
            log.info { "JWT 토큰 타입이 일치하지 않습니다. (expected: ${expectedType.name}, actual: $tokenType)" }
            return false
        }
        return true
    }

    fun getAuthentication(token: String): Authentication {
        val tokenType = getTokenType(getClaims(token))
        if (tokenType.isNotEqualTo(TokenType.ACCESS)) {
            throw InvalidJwtException("Access Token이 아닌 토큰으로는 인증할 수 없습니다. (token_type: $tokenType)")
        }

        val jwtUserInfo: JwtUserInfo = getUserInfo(token)

        val userDetails = SecurityUserDetails(
            userId = jwtUserInfo.userId,
            email = jwtUserInfo.email,
            password = "",
            authorities = listOf(
                SimpleGrantedAuthority(jwtUserInfo.role)
            ),
        )

        return UsernamePasswordAuthenticationToken(
            userDetails,
            token,
            userDetails.authorities
        )
    }

    fun getUserInfo(token: String): JwtUserInfo {
        val claims = getClaims(token)

        val userId = getClaims(token).subject.toLong()
        val email = claims.get("email", String::class.java)
        val role = claims.get("role", String::class.java)

        return JwtUserInfo(
            userId = userId,
            email = email,
            role = role,
        )
    }

    private fun getTokenType(claims: Claims): TokenType {
        val type = claims.get("token_type", String::class.java)
        return TokenType.valueOf(type)
    }

    private fun getClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: JwtException) {
            throw InvalidJwtException("유효하지 않은 JWT 토큰", e)
        } catch (e: Exception) {
            throw InvalidJwtException("JWT 토큰 파싱 에러", e)
        }
    }
}
