package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.application.command.SignUpCommand
import yjh.ontongsal.authapi.domain.ChangePasswordInfo
import yjh.ontongsal.authapi.domain.LoginInfo
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserRegistration
import yjh.ontongsal.authapi.domain.UserRole
import yjh.ontongsal.authapi.infrastructure.UserRepository
import yjh.ontongsal.authapi.shared.response.AppException
import yjh.ontongsal.authapi.shared.response.ErrorCode
import java.time.Instant

@Component
class UserManager(
    // implement layer
    private val userReader: UserReader,
    private val credentialEncoder: CredentialEncoder,

    // infrastructure layer
    private val userRepository: UserRepository
) {

    fun validateDuplicateEmail(email: String) {
        val user = userReader.find(email)
        if (user != null) {
            throw AppException.Conflict(ErrorCode.USER_CONFLICT)
        }
    }

    fun register(signUpCommand: SignUpCommand) {
        val hashedPassword = credentialEncoder.hash(signUpCommand.password)
            ?: throw AppException.BadRequest(ErrorCode.NOT_MATCH_PASSWORD)

        val userRegistration = UserRegistration.of(
            email = signUpCommand.email,
            password = hashedPassword,
            role = UserRole.USER,
            now = Instant.now()
        )
        userRepository.save(userRegistration)
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
            throw AppException.BadRequest(ErrorCode.NOT_MATCH_PASSWORD)
        }

        val hashedPassword = credentialEncoder.hash(changePasswordInfo.newPassword)
            ?: throw AppException.BadRequest(ErrorCode.NOT_MATCH_PASSWORD)

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