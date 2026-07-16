package yjh.ontongsal.authapi.application.command

import yjh.ontongsal.authapi.domain.AuthErrorCode
import yjh.ontongsal.authapi.shared.response.AppException

data class ChangePasswordCommand(
    val currentPassword: String,
    val newPassword: String,
) {
    init {
        require(PASSWORD_REGEX.matches(currentPassword)) {
            throw AppException.BadRequest(AuthErrorCode.INVALID_FORMAT_PASSWORD)
        }
        require(PASSWORD_REGEX.matches(newPassword)) {
            throw AppException.BadRequest(AuthErrorCode.INVALID_FORMAT_PASSWORD)
        }
    }

    companion object {
        private val PASSWORD_REGEX =
            Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")
    }
}