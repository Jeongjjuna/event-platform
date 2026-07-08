package yjh.ontongsal.authapi.presentation

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import yjh.ontongsal.authapi.application.LoginService
import yjh.ontongsal.authapi.application.SignUpService
import yjh.ontongsal.authapi.shared.response.ApiController
import yjh.ontongsal.authapi.shared.response.ApiResponseEntity
import yjh.ontongsal.authapi.shared.security.SecurityUserDetails

@RequestMapping("/api/{version}/users")
@RestController
class UserController(
    private val signUpService: SignUpService,
    private val loginService: LoginService,
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

    @GetMapping(value = ["/me"], version = "v1")
    fun getMyInfo(
        @AuthenticationPrincipal principal: SecurityUserDetails,
    ): ApiResponseEntity<MyInfoResponse> {
        val user = loginService.getMyInfo(principal.userId, principal.username) // username == email
        return ok(MyInfoResponse.from(user))
    }
}