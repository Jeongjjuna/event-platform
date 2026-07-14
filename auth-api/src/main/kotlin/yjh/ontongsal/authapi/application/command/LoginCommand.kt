package yjh.ontongsal.authapi.application.command

import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode

data class LoginCommand(
    val email: String,
    val password: String,
) {
    init {
        require(!EMAIL_REGEX.matches(email)) {
            throw AppException.BadRequest(ErrorCode.INVALID_FORMAT_EMAIL)
        }
        require(!PASSWORD_REGEX.matches(password)) {
            throw AppException.BadRequest(ErrorCode.INVALID_FORMAT_PASSWORD)
        }
    }

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

        private val PASSWORD_REGEX =
            Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")
    }
}