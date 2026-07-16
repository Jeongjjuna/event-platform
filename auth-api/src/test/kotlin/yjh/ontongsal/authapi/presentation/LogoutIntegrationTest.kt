package yjh.ontongsal.authapi.presentation

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.json.JsonMapper
import yjh.ontongsal.authapi.application.CredentialEncoder
import yjh.ontongsal.authapi.config.KotestIntegrationTest
import yjh.ontongsal.authapi.domain.UserRole
import yjh.ontongsal.authapi.infrastructure.UserSessionTable
import yjh.ontongsal.authapi.infrastructure.UserTable
import yjh.ontongsal.authapi.shared.response.SuccessResponse
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider
import yjh.ontongsal.authapi.shared.security.jwt.JwtUserInfo
import yjh.ontongsal.authapi.support.readBody
import java.time.Instant

/**
 * HTTP 명세 + 비즈니스 예외 테스트
 * JWT 생성 여부 및 DB 저장 여부는 검증하지 않는다.
 */
@DisplayName("[통합테스트] 로그아웃 API 테스트")
@KotestIntegrationTest
class LogoutIntegrationTest(
    private val mockMvcTester: MockMvcTester,
    private val jsonMapper: JsonMapper,
    private val jwtTokenProvider: JwtTokenProvider,
    private val credentialEncoder: CredentialEncoder,
) : DescribeSpec({

    fun logoutAPI(accessToken: String) =
        mockMvcTester
            .post()
            .uri("/api/{version}/users/logout", "v1")
            .header("Authorization", "Bearer $accessToken")
            .exchange()

    fun cleanDatabase() = transaction {
        UserSessionTable.deleteAll()
        UserTable.deleteAll()
    }

    fun dbInsertUser(
        email: String = "test@example.com",
        password: String = credentialEncoder.hash("abcd1234!@"),
        userRole: UserRole = UserRole.USER,
        lastLoginAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
        deletedAt: Instant? = null,
    ):Long = transaction {
        UserTable.insert {
            it[UserTable.email] = email
            it[UserTable.password] = credentialEncoder.hash(password)
            it[UserTable.userRole] = userRole.name
            it[UserTable.lastLoginAt] = lastLoginAt
            it[UserTable.createdAt] = createdAt
            it[UserTable.updatedAt] = updatedAt
            it[UserTable.deletedAt] = deletedAt
        } get UserTable.id
    }

    describe("POST /api/v1/users/logout") {
        beforeTest { cleanDatabase() }

        context("유효한 로그인 사용자가 로그아웃 요청하면") {
            it("200 응답을 반환한다.") {
                // given
                val email = "test@example.com"
                val userId = dbInsertUser(
                    email = email,
                    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                )
                val accessToken = jwtTokenProvider.issueAccessToken(JwtUserInfo(userId = userId, email = email, role = UserRole.USER.name,))

                // when
                val result = logoutAPI(accessToken)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.OK.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 200, message = "OK", data = null)
            }
        }
    }
})