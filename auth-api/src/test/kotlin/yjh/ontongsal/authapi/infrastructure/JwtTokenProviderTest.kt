package yjh.ontongsal.authapi.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import yjh.ontongsal.authapi.config.IntegrationTest
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserRole
import yjh.ontongsal.authapi.shared.security.jwt.InvalidJwtException
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider
import yjh.ontongsal.authapi.shared.security.jwt.TokenType
import java.security.KeyPairGenerator
import java.time.Instant
import java.util.*

@DisplayName("[통합테스트] TokenProvider")
class JwtTokenProviderTest @Autowired constructor(
    private val sut: JwtTokenProvider
) : IntegrationTest() {

    @Test
    fun `비대칭키 발급`() {
        // 1. RSA 알고리즘으로 2048비트 키 생성기 초기화
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // RS256 요구 사양

        // 2. 키 쌍 생성
        val keyPair = keyPairGenerator.generateKeyPair()

        // 3. 자바 표준 스펙(Private: PKCS#8, Public: X.509)의 바이트 배열을 Base64 문자열로 변환
        val privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.private.encoded)
        val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)

        // 4. application.yml에 바로 붙여넣을 수 있게 출력
        println("\n=== 📋 [복사용] application.yml 설정 내용 ===")
        println("jwt:")
        println("  private-key: \"$privateKeyBase64\"")
        println("  public-key: \"$publicKeyBase64\"")
        println("===========================================\n")
    }

    @Test
    fun `AccessToken을 정상적으로 발급하고 검증에 성공한다`() {
        // given
        val user = createDummyUser()

        // when
        val token = sut.issueAccessToken(user)
        val isValid = sut.validateToken(token)

        // then
        assertAll(
            { assertNotNull(token) },
            { assertThat(isValid).isEqualTo(true) }
        )
    }

    @Test
    fun `RefreshToken을 정상적으로 발급하고 검증에 성공한다`() {
        // given
        val user = createDummyUser()

        // when
        val token = sut.issueRefreshToken(user)
        val isValid = sut.validateToken(token)

        // then
        assertAll(
            { assertNotNull(token) },
            { assertThat(isValid).isEqualTo(true) }
        )
    }

    @Test
    fun `AccessToken은 REFRESH 타입 검증에 실패한다`() {
        // given
        val user = createDummyUser()
        val accessToken = sut.issueAccessToken(user)

        // when & then
        assertAll(
            { assertThat(sut.validateToken(accessToken, TokenType.ACCESS)).isEqualTo(true) },
            { assertThat(sut.validateToken(accessToken, TokenType.REFRESH)).isEqualTo(false) }
        )
    }

    @Test
    fun `RefreshToken은 ACCESS 타입 검증에 실패한다`() {
        // given
        val user = createDummyUser()
        val refreshToken = sut.issueRefreshToken(user)

        // when & then
        assertAll(
            { assertThat(sut.validateToken(refreshToken, TokenType.REFRESH)).isEqualTo(true) },
            { assertThat(sut.validateToken(refreshToken, TokenType.ACCESS)).isEqualTo(false) }
        )
    }

    @Test
    fun `RefreshToken으로는 인증 객체를 만들 수 없다`() {
        // given
        val user = createDummyUser()
        val refreshToken = sut.issueRefreshToken(user)

        // when & then
        assertThrows<InvalidJwtException> {
            sut.getAuthentication(refreshToken)
        }
    }

    @Test
    fun `위조되거나 변형된 토큰은 검증에 실패한다`() {
        // given
        val user = createDummyUser()
        val originalToken = sut.issueAccessToken(user)
        val tamperedToken = originalToken + "X"

        // when
        val isValid = sut.validateToken(tamperedToken)

        // then
        assertThat(isValid).isEqualTo(false)
    }

    private fun createDummyUser(): User {
        return User(
            id = 1,
            email = "test@example.com",
            password = "encodedPassword123!",
            role = UserRole.USER,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            deletedAt = null
        )
    }
}