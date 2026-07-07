package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.SignUpInfo
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.infrastructure.UserRepository
import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode

@Component
class UserManager(
    // implement layer
    private val userReader: UserReader,
    private val credentialEncoder: CredentialEncoder,

    // infrastructure layer
    private val userRepository: UserRepository
) {

    fun checkAlreadyRegistered(email: String) {
        val user = userReader.read(email)
        if (user != null) {
            throw AppException.Conflict(ErrorCode.USER_CONFLICT)
        }
    }

    fun signUp(signUpInfo: SignUpInfo) {
        val hashedPassword = credentialEncoder.hash(signUpInfo.password)
            ?: throw AppException.BadRequest(ErrorCode.INVALID_PASSWORD)

        val user = User.signUp(
            email = signUpInfo.email,
            password = hashedPassword
        )
        userRepository.save(user)
    }
}