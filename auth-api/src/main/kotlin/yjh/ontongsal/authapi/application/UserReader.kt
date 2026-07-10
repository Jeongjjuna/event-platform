package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.infrastructure.UserRepository

@Component
class UserReader(
    // infrastructure layer
    private val userRepository: UserRepository
) {

    fun read(email: String): User? =
        userRepository.findByEmail(email)
}