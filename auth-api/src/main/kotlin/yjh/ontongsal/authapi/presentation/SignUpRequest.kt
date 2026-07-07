package yjh.ontongsal.authapi.presentation

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import yjh.ontongsal.authapi.domain.SignUpInfo

data class SignUpRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "비밀번호는 영문자와 숫자를 포함한 8자리 이상이어야 합니다."
    )
    val password: String,
) {

    fun toSignUpInfo(): SignUpInfo {
        return SignUpInfo(email, password)
    }
}
