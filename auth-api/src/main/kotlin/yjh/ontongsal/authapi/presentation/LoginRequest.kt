package yjh.ontongsal.authapi.presentation

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import yjh.ontongsal.authapi.domain.LoginInfo

data class LoginRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    val password: String,
) {

    fun toLoginInfo(): LoginInfo {
        return LoginInfo(email, password)
    }
}
