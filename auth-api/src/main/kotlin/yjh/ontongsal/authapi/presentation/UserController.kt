package yjh.ontongsal.authapi.presentation

import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import yjh.ontongsal.authapi.application.*
import yjh.ontongsal.authapi.presentation.request.ChangePasswordRequest
import yjh.ontongsal.authapi.presentation.request.LoginRequest
import yjh.ontongsal.authapi.presentation.request.SignUpRequest
import yjh.ontongsal.authapi.presentation.response.LoginResponse
import yjh.ontongsal.authapi.presentation.response.MyInfoResponse
import yjh.ontongsal.authapi.presentation.response.RefreshResponse
import yjh.ontongsal.authapi.shared.response.ApiController
import yjh.ontongsal.authapi.shared.response.ApiResponseEntity
import yjh.ontongsal.authapi.shared.security.SecurityUserDetails
import java.time.Duration

@RequestMapping("/api/{version}/users")
@RestController
class UserController(
    private val signUpService: SignUpService,
    private val loginService: LoginService,
    private val refreshService: RefreshService,
    private val logoutService: LogoutService,
    private val changePasswordService: ChangePasswordService,
    private val withdrawService: WithdrawService,
) : ApiController {

    @PostMapping(value = ["/signup"], version = "v1")
    fun signUp(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ): ApiResponseEntity<Unit> {
        signUpService.signUp(signUpRequest.toCommand())
        return ok()
    }

    @PostMapping(value = ["/login"], version = "v1")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest
    ): ApiResponseEntity<LoginResponse> {
        val loginResult = loginService.login(loginRequest.toCommand())

        // refresh token 은 http only 헤더를 통해 쿠키 저장.
        val cookie = ResponseCookie.from("refreshToken", loginResult.jwtToken.refreshToken)
            .httpOnly(true)
            .secure(false) // 운영에서 true
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build()

        val headers = HttpHeaders().apply {
            add(HttpHeaders.SET_COOKIE, cookie.toString())
        }
        return ok(data = LoginResponse.from(loginResult), headers = headers)
    }

    @PostMapping(value = ["/refresh"], version = "v1")
    fun refresh(
        @CookieValue("refreshToken") refreshToken: String,
    ): ApiResponseEntity<RefreshResponse> {
        val refreshResult = refreshService.refreshToken(refreshToken)

        val cookie = ResponseCookie.from("refreshToken", refreshResult.jwtToken.refreshToken)
            .httpOnly(true)
            .secure(false) // 운영에서 true
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build()
        val headers = HttpHeaders().apply {
            add(HttpHeaders.SET_COOKIE, cookie.toString())
        }
        return ok(data = RefreshResponse.from(refreshResult), headers = headers)
    }


    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = ["/logout"], version = "v1")
    fun logout(
        @AuthenticationPrincipal principal: SecurityUserDetails,
    ): ApiResponseEntity<Unit> {
        logoutService.logout(principal.userId)

        val cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false) // 로그인과 동일
            .sameSite("Strict")
            .path("/")     // 로그인과 동일
            .maxAge(Duration.ZERO)
            .build()

        val headers = HttpHeaders().apply {
            add(HttpHeaders.SET_COOKIE, cookie.toString())
        }
        return ok(headers = headers)
    }

    @PreAuthorize("hasAuthority('USER')")
    @PatchMapping(value = ["/me/password"], version = "v1")
    fun changePassword(
        @AuthenticationPrincipal principal: SecurityUserDetails,
        @Valid @RequestBody changePasswordRequest: ChangePasswordRequest,
    ): ApiResponseEntity<Unit> {
        changePasswordService.changePassword(principal.userId, principal.username, changePasswordRequest.toCommand())
        return ok()
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping(value = ["/me"], version = "v1")
    fun withdraw(
        @AuthenticationPrincipal principal: SecurityUserDetails,
    ): ApiResponseEntity<Unit> {
        withdrawService.withdraw(principal.userId, principal.username)
        return ok()
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(value = ["/me"], version = "v1")
    fun getMyInfo(
        @AuthenticationPrincipal principal: SecurityUserDetails,
    ): ApiResponseEntity<MyInfoResponse> {
        val cachedUser = loginService.getMyInfo(principal.userId, principal.username) // username == email
        return ok(MyInfoResponse.from(cachedUser))
    }
}