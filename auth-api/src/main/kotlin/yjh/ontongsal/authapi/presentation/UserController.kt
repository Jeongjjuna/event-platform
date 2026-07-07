package yjh.ontongsal.authapi.presentation

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yjh.ontongsal.authapi.application.SignUpService
import yjh.ontongsal.authapi.shared.response.ApiController
import yjh.ontongsal.authapi.shared.response.ApiResponseEntity

@RequestMapping("/api/{version}/users")
@RestController
class UserController(
    private val signUpService: SignUpService
) : ApiController {

    @PostMapping(value = ["/signup"], version = "v1")
    fun signUp(
        @Valid @RequestBody signUpRequest: SignUpRequest
    ): ApiResponseEntity<Unit> {
        signUpService.signUp(signUpRequest.toSignUpInfo())
        return ok()
    }
}