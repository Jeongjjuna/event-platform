package yjh.ontongsal.authapi.presentation

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.json.JsonMapper
import yjh.ontongsal.authapi.application.CredentialEncoder
import yjh.ontongsal.authapi.config.KotestIntegrationTest
import yjh.ontongsal.authapi.domain.UserRole
import yjh.ontongsal.authapi.infrastructure.UserSessionTable
import yjh.ontongsal.authapi.infrastructure.UserTable
import yjh.ontongsal.authapi.presentation.request.ChangePasswordRequest
import yjh.ontongsal.authapi.shared.response.SuccessResponse
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider
import yjh.ontongsal.authapi.shared.security.jwt.JwtUserInfo
import yjh.ontongsal.authapi.support.readBody
import java.time.Instant

/**
 * HTTP 명세 + 비즈니스 예외 테스트
 * 비밀번호 변경 이후 저장 여부는 검증하지 않는다.
 */
@DisplayName("[통합테스트] 비밀번호 변경 API 테스트")
@KotestIntegrationTest
class PasswordUpdateIntegrationTest(
    private val mockMvcTester: MockMvcTester,
    private val jsonMapper: JsonMapper,
    private val jwtTokenProvider: JwtTokenProvider,
    private val credentialEncoder: CredentialEncoder,
) : DescribeSpec({

    val payload = ChangePasswordRequest(
        currentPassword = "oldPassword123!",
        newPassword = "newPassword123!"
    )

    fun changePasswordAPI(
        accessToken: String,
        payload: ChangePasswordRequest,
    ) =
        mockMvcTester
            .patch()
            .uri("/api/{version}/users/me/password", "v1")
            .header("Authorization", "Bearer $accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(payload))
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

    describe("PATCH /api/v1/users/me/password") {
        beforeTest { cleanDatabase() }

        context("유효한 비밀번호 변경 정보를 입력한 경우") {
            it("200 응답을 반환한다.") {
                // given
                // 요청할 currentPassword와 동일한 비밀번호로 유저를 미리 저장
                val email = "test@example.com"
                dbInsertUser(
                    email = email,
                    password = payload.currentPassword,
                )

                // 저장된 유저(userId = 1L) 기준으로 accessToken 발급
                val accessToken = jwtTokenProvider.issueAccessToken(
                    JwtUserInfo(
                        userId = 1L,
                        email = email,
                        role = UserRole.USER.name,
                    )
                )

                // when
                val result = changePasswordAPI(accessToken, payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.OK.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 200, message = "OK", data = null)
            }
        }

        context("현재 비밀번호가 일치하지 않는 경우") {
            it("400 응답을 반환한다.") {
                // given
                // 유저의 실제 비밀번호는 payload.currentPassword로 저장
                val email = "test@example.com"
                dbInsertUser(
                    email = email,
                    password = payload.currentPassword,
                )

                val accessToken = jwtTokenProvider.issueAccessToken(
                    JwtUserInfo(
                        userId = 1L,
                        email = email,
                        role = UserRole.USER.name,
                    )
                )

                // 요청 시엔 currentPassword를 일부러 틀리게 보냄 (= 비밀번호 불일치 상황 재현)
                val invalidPasswordPayload = payload.copy(
                    currentPassword = "wrongPassword123!"
                )

                // when
                val result = changePasswordAPI(accessToken, invalidPasswordPayload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1002, message = "비밀번호가 일치하지 않습니다.", data = null,)
            }
        }

        context("이미 탈퇴한 유저가 비밀번호 변경 요청하면") {
            it("404 응답을 반환한다.") {
                // given
                // 비밀번호는 정상 일치하도록 저장하되, deletedAt을 채워서 탈퇴 상태로 만듦
                val email = "test@example.com"
                dbInsertUser(
                    email = email,
                    password = payload.currentPassword,
                    deletedAt = Instant.now(),
                )

                // 탈퇴한 유저와 동일한 userId(1L)로 accessToken 발급
                // (인증 자체는 통과시키고, "탈퇴 후 조회 실패"만 검증하려는 의도)
                val accessToken = jwtTokenProvider.issueAccessToken(
                    JwtUserInfo(
                        userId = 1L,
                        email = email,
                        role = UserRole.USER.name,
                    )
                )

                // when
                val result = changePasswordAPI(accessToken, payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.NOT_FOUND.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1000, message = "사용자를 찾을 수 없습니다.", data = null)
            }
        }
    }
})