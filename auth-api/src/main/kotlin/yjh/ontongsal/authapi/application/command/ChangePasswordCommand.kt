package yjh.ontongsal.authapi.application.command

import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode

data class ChangePasswordCommand(
    val currentPassword: String,
    val newPassword: String,
) {
    init {
        require(!PASSWORD_REGEX.matches(currentPassword)) {
            throw AppException.BadRequest(ErrorCode.INVALID_FORMAT_PASSWORD)
        }
        require(!PASSWORD_REGEX.matches(newPassword)) {
            throw AppException.BadRequest(ErrorCode.INVALID_FORMAT_PASSWORD)
        }
    }

    companion object {
        private val PASSWORD_REGEX =
            Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")
    }
}