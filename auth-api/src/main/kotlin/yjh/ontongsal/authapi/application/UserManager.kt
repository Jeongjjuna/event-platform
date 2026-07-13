package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.ChangePasswordInfo
import yjh.ontongsal.authapi.domain.LoginInfo
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
        val user = userReader.find(email)
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

    fun authenticate(loginInfo: LoginInfo): User {
        val user = userReader.find(loginInfo.email)
            ?: throw AppException.Unauthorized(ErrorCode.LOGIN_FAILED)

        if (!credentialEncoder.matches(loginInfo.password, user.password)) {
            throw AppException.Unauthorized(ErrorCode.LOGIN_FAILED)
        }
        return user
    }

    fun changePassword(email: String, changePasswordInfo: ChangePasswordInfo) {
        val user = userReader.read(email)

        if (!credentialEncoder.matches(changePasswordInfo.currentPassword, user.password)) {
            throw AppException.BadRequest(ErrorCode.INVALID_PASSWORD)
        }

        val hashedPassword = credentialEncoder.hash(changePasswordInfo.newPassword)
            ?: throw AppException.BadRequest(ErrorCode.INVALID_PASSWORD)

        user.changePassword(hashedPassword)
        userRepository.updatePassword(user)
    }

    fun withdraw(email: String) {
        val user = userReader.read(email)
        user.withdraw()
        val deleted = userRepository.softDelete(user)
        if (deleted == 0) {
            throw AppException.NotFound(ErrorCode.USER_NOT_FOUND)
        }
    }
}