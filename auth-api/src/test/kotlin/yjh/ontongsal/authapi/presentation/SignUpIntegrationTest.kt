package yjh.ontongsal.authapi.presentation

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
import yjh.ontongsal.authapi.presentation.request.SignUpRequest
import yjh.ontongsal.authapi.shared.response.SuccessResponse
import yjh.ontongsal.authapi.support.readBody
import java.time.Instant


/**
 * HTTP 명세 + 비즈니스 예외 테스트
 * 기타 인프라 발급 및 저장 여부는 검증 x
 */
@DisplayName("[통합테스트] 회원 가입 API 테스트")
@KotestIntegrationTest
class SignUpIntegrationTest(
    private val mockMvcTester: MockMvcTester,
    private val jsonMapper: JsonMapper,
    private val credentialEncoder: CredentialEncoder,
) : DescribeSpec({

    val payload = SignUpRequest(
        email = "test@example.com",
        password = "abcd1234!@"
    )

    fun signUpAPI(payload: SignUpRequest) = mockMvcTester
        .post()
        .uri("/api/{version}/users/signup", "v1")
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

    fun dbInsertUser(email: String) = transaction {
        UserTable.insert {
            it[UserTable.email] = email
            it[password] = credentialEncoder.hash("abcd1234!@")
            it[userRole] = UserRole.USER.name
            it[lastLoginAt] = null
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
            it[deletedAt] = null
        }
    }

    describe("POST /api/v1/users/signup") {
        beforeTest { cleanDatabase() }

        context("유효한 회원가입 정보를 입력한 경우") {
            it("200 응답을 반환한다.") {
                // given
                // 별도 준비 없음 (= 이메일이 아직 존재하지 않는 정상 케이스)

                // when
                val result = signUpAPI(payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.OK.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 200, message = "OK", data = null)
            }
        }

        context("중복된 이메일로 회원가입을 시도하면") {
            it("409 응답을 반환한다.") {
                // given
                // 요청과 동일한 이메일로 유저를 미리 저장 (= 중복 상황 재현)
                dbInsertUser(payload.email)

                // when
                val result = signUpAPI(payload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.CONFLICT.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1001, message = "사용자가 이미 존재합니다.", data = null)
            }
        }

        context("유효하지 않은 이메일 형식이라면") {
            it("400 응답을 반환한다.") {
                // given
                // 이메일 형식만 일부러 깨뜨린 payload (DB 준비는 필요 없음 - 형식 검증에서 먼저 걸러짐)
                val invalidEmailPayload = payload.copy(email = "invalid-email")

                // when
                val result = signUpAPI(invalidEmailPayload)

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
                val invalidPasswordPayload = payload.copy(password = "invalid-password")

                // when
                val result = signUpAPI(invalidPasswordPayload)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1005, message = "유효하지 않은 형식의 비밀번호 입니다.", data = null)
            }
        }
    }

})