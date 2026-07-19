package yjh.ontongsal.authapi.presentation

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.provided.ApiReportContext
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
import yjh.ontongsal.authapi.presentation.request.LoginRequest
import yjh.ontongsal.authapi.presentation.response.LoginResponse
import yjh.ontongsal.authapi.shared.response.SuccessResponse
import yjh.ontongsal.authapi.support.readBody
import java.time.Instant

/**
 * HTTP 명세 + 비즈니스 예외 테스트
 * JWT 생성 여부 및 DB 저장 여부는 검증하지 않는다.
 */
@DisplayName("[통합테스트] 로그인 API 테스트")
@KotestIntegrationTest
class LoginIntegrationTest(
    private val mockMvcTester: MockMvcTester,
    private val jsonMapper: JsonMapper,
    private val credentialEncoder: CredentialEncoder,
) : DescribeSpec({

    val payload = LoginRequest(
        email = "test@example.com",
        password = "abcd1234!@"
    )

    fun loginAPI(payload: LoginRequest) = mockMvcTester
            .post()
            .uri("/api/{version}/users/login", "v1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(payload))
            .exchange()
        .also {
            ApiReportContext.record(it)
        }

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

    describe("POST /api/v1/users/login") {
        beforeTest { cleanDatabase() }

        context("유효한 로그인 정보를 입력한 경우") {
            it("200 응답을 반환한다.") {
                // given
                // 요청할 password와 동일한 비밀번호로 유저를 미리 저장
                val userId = dbInsertUser(
                    email = payload.email,
                    password = payload.password,
                    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                )

                // when
                val result = loginAPI(payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.OK.value()
                val response = result.readBody<SuccessResponse<LoginResponse>>(jsonMapper)

                // 방법1.
                with(response) {
                    code shouldBe 200
                    message shouldBe "OK"
                    with(data.shouldNotBeNull()) {
                        accessToken shouldNotBe ""
                        with(user.shouldNotBeNull()) {
                            id shouldBe 1L
                            email shouldBe payload.email
                            role shouldBe UserRole.USER
                            registeredAt shouldBe Instant.parse("2026-01-01T00:00:00Z")
                        }
                    }
                }
                // 방법2.
//                response.code shouldBe 200
//                response.message shouldBe "OK"
//                val data = response.data.shouldNotBeNull()
//
//                data.accessToken shouldNotBe ""
//                data.refreshToken shouldNotBe ""
//                val user = data.user.shouldNotBeNull()
//
//                user.id shouldBe 1L
//                user.email shouldBe payload.email
//                user.role shouldBe UserRole.USER
//                user.registeredAt shouldBe Instant.parse("2026-01-01T00:00:00Z")
            }
        }

        context("유효하지 않은 이메일 형식이라면") {
            it("400 응답을 반환한다.") {
                // given
                // 이메일 형식만 일부러 깨뜨린 payload (DB 준비는 필요 없음 - 형식 검증에서 먼저 걸러짐)
                val invalidEmailPayload = payload.copy(email = "invalid-email")

                // when
                val result = loginAPI(invalidEmailPayload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1004, message = "유효하지 않은 형식의 이메일 입니다.", data = null)
            }
        }

        context("유효하지 않은 비밀번호 형식이라면") {
            it("400 응답을 반환한다.") {
                // given
                // 비밀번호 형식만 일부러 깨뜨린 payload (DB 준비는 필요 없음 - 형식 검증에서 먼저 걸러짐)
                val invalidPasswordPayload = payload.copy(password = "short")

                // when
                val result = loginAPI(invalidPasswordPayload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1005, message = "유효하지 않은 형식의 비밀번호 입니다.", data = null)
            }
        }

        context("존재하지 않는 이메일로 로그인하면") {
            it("404 응답을 반환한다.") {
                // given
                // 유저를 아예 저장하지 않음 (= 존재하지 않는 이메일 상황을 그대로 재현)

                // when
                val result = loginAPI(payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.NOT_FOUND.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1000, message = "사용자를 찾을 수 없습니다.", data = null)
            }
        }

        context("비밀번호가 일치하지 않으면") {
            it("401 응답을 반환한다.") {
                // given
                // 유저는 저장하되, 요청 시 비밀번호만 틀리게 보냄
                dbInsertUser(payload.email)
                val wrongPasswordPayload = payload.copy(password = "wrong-password1234!@")

                // when
                val result = loginAPI(wrongPasswordPayload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1002, message = "비밀번호가 일치하지 않습니다.", data = null)
            }
        }

        context("이미 탈퇴한 유저가 로그인 요청하면") {
            it("404 응답을 반환한다.") {
                // given
                // 유저는 저장하되 deletedAt을 채워서 탈퇴 상태로 만듦
                dbInsertUser(payload.email, deletedAt = Instant.now())

                // when
                val result = loginAPI(payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.NOT_FOUND.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1000, message = "사용자를 찾을 수 없습니다.", data = null)
            }
        }
    }
})