package yjh.ontongsal.authapi.presentation

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import yjh.ontongsal.authapi.application.LoginService
import yjh.ontongsal.authapi.application.LogoutService
import yjh.ontongsal.authapi.application.RefreshService
import yjh.ontongsal.authapi.application.SignUpService
import yjh.ontongsal.authapi.shared.response.ApiController
import yjh.ontongsal.authapi.shared.response.ApiResponseEntity
import yjh.ontongsal.authapi.shared.security.SecurityUserDetails

@RequestMapping("/api/{version}/users")
@RestController
class UserController(
    private val signUpService: SignUpService,
    private val loginService: LoginService,
    private val refreshService: RefreshService,
    private val logoutService: LogoutService,
) : ApiController {

    @PostMapping(value = ["/signup"], version = "v1")
    fun signUp(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ): ApiResponseEntity<Unit> {
        signUpService.signUp(signUpRequest.toSignUpInfo())
        return ok()
    }

    @PostMapping(value = ["/login"], version = "v1")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest
    ): ApiResponseEntity<LoginResponse> {
        val loginResult = loginService.login(loginRequest.toLoginInfo())
        return ok(LoginResponse.from(loginResult))
    }

    @PostMapping(value = ["/refresh"], version = "v1")
    fun refresh(
        @Valid @RequestBody refreshRequest: RefreshRequest
    ): ApiResponseEntity<RefreshResponse> {
        val (accessToken, refreshToken) = refreshService.refreshToken(refreshRequest.refreshToken)
        return ok(RefreshResponse.from(accessToken, refreshToken))
    }


    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = ["/logout"], version = "v1")
    fun logout(
        @AuthenticationPrincipal principal: SecurityUserDetails,
    ): ApiResponseEntity<Unit> {
        logoutService.logout(principal.userId)
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