package yjh.ontongsal.authapi.presentation

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.provided.ApiReportContext
import jakarta.servlet.http.Cookie
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
import yjh.ontongsal.authapi.presentation.response.RefreshResponse
import yjh.ontongsal.authapi.shared.response.SuccessResponse
import yjh.ontongsal.authapi.shared.security.jwt.JwtTokenProvider
import yjh.ontongsal.authapi.shared.security.jwt.JwtUserInfo
import yjh.ontongsal.authapi.support.readBody
import java.time.Instant


/**
 * HTTP 명세 + 비즈니스 예외 테스트
 * JWT 생성 여부 및 세션 저장 여부는 검증하지 않는다.
 */
@DisplayName("[통합테스트] 세션 갱신 API 테스트")
@KotestIntegrationTest
class SessionRefreshIntegrationTest(
    private val mockMvcTester: MockMvcTester,
    private val jsonMapper: JsonMapper,
    private val jwtTokenProvider: JwtTokenProvider,
    private val credentialEncoder: CredentialEncoder,
) : DescribeSpec({

    fun tokenRefreshAPI(refreshToken: String) =
        mockMvcTester
            .post()
            .uri("/api/{version}/users/refresh", "v1")
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(Cookie("refreshToken", refreshToken))
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

    fun dbInsertSession(
        userId: Long,
        refreshToken: String,
        updatedAt: Instant = Instant.now(),
    ) = transaction {
        UserSessionTable.insert {
            it[UserSessionTable.userId] = userId
            it[UserSessionTable.refreshToken] = refreshToken
            it[UserSessionTable.updatedAt] = updatedAt
        }
    }

    describe("POST /api/v1/users/refresh") {
        beforeTest { cleanDatabase() }

        context("유효한 인증 세션으로 갱신 요청한 경우") {
            it("200 응답을 반환한다.") {
                // given
                // 1. 유저를 DB에 저장
                val email = "test@example.com"
                val userId = dbInsertUser(email)

                // 2. 해당 유저 기준으로 access/refresh token 발급
                val accessToken = jwtTokenProvider.issueAccessToken(JwtUserInfo(userId, email, UserRole.USER.name))
                val refreshToken = jwtTokenProvider.issueRefreshToken(JwtUserInfo(userId, email, UserRole.USER.name))

                // 3. 발급한 refreshToken을 세션 테이블에도 저장 (정상 케이스: 요청 토큰 == 저장된 토큰)
                dbInsertSession(userId = userId, refreshToken = refreshToken)

                // when
                val result = tokenRefreshAPI(refreshToken)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.OK.value()
                val response = result.readBody<SuccessResponse<RefreshResponse>>(jsonMapper)
                with(response) {
                    code shouldBe 200
                    message shouldBe "OK"
                    with(data.shouldNotBeNull()) {
                        accessToken shouldNotBe ""
                        refreshToken shouldNotBe ""
                    }
                }
            }
        }

        context("유효하지 않은 refresh token이라면") {
            it("401 응답을 반환한다.") {
                // given
                // 1. 유저를 DB에 저장
                val email = "test@example.com"
                val userId = dbInsertUser(email)

                // 2. 정상 refreshToken을 발급하고 세션에도 저장 (하지만 실제 요청 시엔 안 씀)
                val refreshToken = jwtTokenProvider.issueRefreshToken(JwtUserInfo(userId, email, UserRole.USER.name))
                dbInsertSession(userId = userId, refreshToken = refreshToken)

                // when
                // 발급받은 정상 토큰이 아니라, 형식 자체가 잘못된 문자열("invalid-token")로 요청
                val result = tokenRefreshAPI("invalid-token")

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 2000, message = "토큰 타입이 유효하지 않습니다.", data = null,)
            }
        }

        context("로그인 세션이 존재하지 않는 유저라면") {
            it("401 응답을 반환한다.") {
                // given
                // 1. 유저를 DB에 저장
                val email = "test@example.com"
                val userId = dbInsertUser(email)

                // 2. refreshToken은 정상 발급하지만
                val refreshToken = jwtTokenProvider.issueRefreshToken(JwtUserInfo(userId, email, UserRole.USER.name))
                // 3. 세션 테이블에는 저장하지 않음 (= 로그인 세션 없음 상태를 의도적으로 재현)
                // session 미등록

                // when
                val result = tokenRefreshAPI(refreshToken)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 2001, message = "세션이 존재하지 않습니다.", data = null,)
            }
        }

        context("요청한 refresh token이 저장된 세션과 다르다면") {
            it("401 응답을 반환한다.") {
                // given
                // 1. 유저를 DB에 저장
                val email = "test@example.com"
                val userId = dbInsertUser(email)

                // 2. 세션 테이블엔 임의의 다른 토큰 문자열("refreshToken")을 저장해둠
                dbInsertSession(userId = userId, refreshToken = "refreshToken")

                // 3. 실제 요청에는 정상적으로 발급된 (하지만 세션에 저장된 값과는 다른) refreshToken 사용
                val differentRefreshToken = jwtTokenProvider.issueRefreshToken(JwtUserInfo(userId, email, UserRole.USER.name))

                // when
                // 세션에 저장된 토큰과 요청 토큰이 불일치하는 상황을 재현
                val result = tokenRefreshAPI(differentRefreshToken)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 2002, message = "인증 정보가 일치하지 않습니다.", data = null,)
            }
        }

        context("refresh token의 사용자가 존재하지 않는다면") {
            it("404 응답을 반환한다.") {
                // given
                // 1. 존재하지 않는 userId(999L)로 refreshToken을 발급
                //    (실제 유저는 DB에 저장하지 않음 → 유저 미존재 상태를 의도적으로 재현)
                val email = "test@example.com"
                val refreshToken = jwtTokenProvider.issueRefreshToken(JwtUserInfo(999L, email, UserRole.USER.name))

                // 2. 세션 테이블에는 존재하지 않는 userId 기준으로 세션을 저장
                //    (세션 검증까지는 통과시키고, "유저 조회 실패"만 검증하려는 의도)
                dbInsertSession(userId = 999L, refreshToken = refreshToken)

                // when
                val result = tokenRefreshAPI(refreshToken)

                // then
                result.response.getHeader("X-Service-Name") shouldBe "auth-api"
                result.response.status shouldBe HttpStatus.NOT_FOUND.value()
                result.readBody<SuccessResponse<Unit>>(jsonMapper) shouldBe
                        SuccessResponse(code = 1000, message = "사용자를 찾을 수 없습니다.", data = null,)
            }
        }
    }
})