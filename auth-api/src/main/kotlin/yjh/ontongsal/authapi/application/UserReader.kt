package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.infrastructure.UserRepository
import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode

@Component
class UserReader(
    // infrastructure layer
    private val userRepository: UserRepository
) {

    fun find(email: String): User? =
        userRepository.findByEmail(email)

    fun read(email: String): User =
        userRepository.findByEmail(email)
            ?: throw AppException.NotFound(ErrorCode.USER_NOT_FOUND)
}