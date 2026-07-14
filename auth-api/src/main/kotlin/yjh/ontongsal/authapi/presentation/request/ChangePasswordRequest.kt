package yjh.ontongsal.authapi.presentation.request

import jakarta.validation.constraints.NotBlank
import yjh.ontongsal.authapi.application.command.ChangePasswordCommand

data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    val currentPassword: String,

    @field:NotBlank(message = "신규 비밀번호는 필수 입력 항목입니다.")
    val newPassword: String,
) {

    fun toCommand(): ChangePasswordCommand {
        return ChangePasswordCommand(currentPassword, newPassword)
    }
}

