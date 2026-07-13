package yjh.ontongsal.authapi.presentation

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import yjh.ontongsal.authapi.domain.ChangePasswordInfo

data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호를 입력해주세요.")
    val currentPassword: String,

    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "비밀번호는 영문자와 숫자를 포함한 8자리 이상이어야 합니다."
    )
    val newPassword: String,
) {

    fun toChangePasswordInfo(): ChangePasswordInfo {
        return ChangePasswordInfo(currentPassword, newPassword)
    }
}

