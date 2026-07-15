package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.AuthErrorCode
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.infrastructure.UserRepository
import yjh.ontongsal.authapi.shared.response.AppException

@Component
class UserReader(
    // infrastructure layer
    private val userRepository: UserRepository
) {

    fun find(email: String): User? =
        userRepository.findByEmail(email)

    fun read(email: String): User =
        userRepository.findByEmail(email)
            ?: throw AppException.NotFound(AuthErrorCode.USER_NOT_FOUND)
}