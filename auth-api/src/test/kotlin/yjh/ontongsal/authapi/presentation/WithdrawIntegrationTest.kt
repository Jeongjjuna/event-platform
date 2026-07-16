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

@DisplayName("[통합테스트] 회원 탈퇴 API 테스트")
@KotestIntegrationTest
class WithdrawIntegrationTest(
    private val mockMvcTester: MockMvcTester,
    private val jsonMapper: JsonMapper,
    private val jwtTokenProvider: JwtTokenProvider,
    private val credentialEncoder: CredentialEncoder,
) : DescribeSpec({

    fun withdrawAPI(accessToken: String) = mockMvcTester
        .delete()
        .uri("/api/{version}/users/me", "v1")
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

    describe("DELETE /api/v1/users/me") {
        beforeTest { cleanDatabase() }

        context("유효한 회원가입 탈퇴 요청이 들어오면") {
            it("200 응답을 반환한다.") {
                // given
                val email = "test@example.com"
                val userId = dbInsertUser(email)
                val accessToken = jwtTokenProvider.issueAccessToken(JwtUserInfo(userId, email, UserRole.USER.name))

                // when
                val result = withdrawAPI(accessToken)

                // then
                result.response.status shouldBe HttpStatus.OK.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 200, message = "OK", data = null)
            }
        }

        context("등록 되지 않은 유저에 대한 탈퇴요청이 들어오면") {
            it("404 응답을 반환한다.") {
                // given
                val nonExistentUserId = 9999L
                val accessToken = jwtTokenProvider.issueAccessToken(JwtUserInfo(nonExistentUserId, "none@example.com", UserRole.USER.name))

                // when
                val result = withdrawAPI(accessToken)

                // then
                result.response.status shouldBe HttpStatus.NOT_FOUND.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1000, message = "사용자를 찾을 수 없습니다.", data = null)
            }
        }

        context("이미 탈퇴한 유저의 회원 탈퇴 요청이 들어오면") {
            it("404 응답을 반환한다.") {
                // given
                val email = "test@example.com"
                val userId = dbInsertUser(email, deletedAt = Instant.now())
                val accessToken = jwtTokenProvider.issueAccessToken(JwtUserInfo(userId, email, UserRole.USER.name))

                // when
                val result = withdrawAPI(accessToken)

                // then
                result.response.status shouldBe HttpStatus.NOT_FOUND.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1000, message = "사용자를 찾을 수 없습니다.", data = null)
            }
        }
    }
})