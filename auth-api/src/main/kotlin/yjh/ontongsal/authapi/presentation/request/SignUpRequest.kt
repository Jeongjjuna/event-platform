package yjh.ontongsal.authapi.presentation.request

import jakarta.validation.constraints.NotBlank
import yjh.ontongsal.authapi.application.command.SignUpCommand

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    val password: String,
) {

    fun toCommand(): SignUpCommand {
        return SignUpCommand(email, password)
    }
}
