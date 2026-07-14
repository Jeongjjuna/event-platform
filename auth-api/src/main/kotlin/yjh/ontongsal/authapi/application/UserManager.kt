package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.application.command.ChangePasswordCommand
import yjh.ontongsal.authapi.application.command.SignUpCommand
import yjh.ontongsal.authapi.domain.AuthErrorCode
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserRegistration
import yjh.ontongsal.authapi.domain.UserRole
import yjh.ontongsal.authapi.infrastructure.UserRepository
import yjh.ontongsal.authapi.shared.response.AppException
import java.time.Instant

@Component
class UserManager(
    // implement layer
    private val userReader: UserReader,
    private val credentialEncoder: CredentialEncoder,

    // infrastructure layer
    private val userRepository: UserRepository,
) {

    fun validateDuplicateEmail(email: String) {
        val user = userReader.find(email)
        if (user != null) {
            throw AppException.Conflict(AuthErrorCode.USER_CONFLICT)
        }
    }

    fun register(signUpCommand: SignUpCommand) {
        val hashedPassword = credentialEncoder.hash(signUpCommand.password)

        val userRegistration = UserRegistration.of(
            email = signUpCommand.email,
            password = hashedPassword,
            role = UserRole.USER,
            now = Instant.now()
        )
        userRepository.save(userRegistration)
    }

    fun login(user: User, password: String) {
        if (!credentialEncoder.matches(user.password, password)) {
            throw AppException.Unauthorized(AuthErrorCode.LOGIN_FAILED)
        }

        user.login(Instant.now());
        userRepository.updateLoginTime(user)
    }

    fun changePassword(user: User, changePasswordCommand: ChangePasswordCommand) {
        if (!credentialEncoder.matches(changePasswordCommand.currentPassword, user.password)) {
            throw AppException.BadRequest(AuthErrorCode.NOT_MATCH_PASSWORD)
        }

        val hashedPassword = credentialEncoder.hash(changePasswordCommand.newPassword)

        user.changePassword(hashedPassword)
        userRepository.updatePassword(user)
    }

    fun withdraw(user: User) {
        userRepository.delete(user)
    }
}