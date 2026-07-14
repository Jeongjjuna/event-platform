package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.application.command.SignUpCommand
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

/**
 * 회원가입 "비즈니스 로직"
 */
@Service
class SignUpService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
) {
    fun signUp(signUpCommand: SignUpCommand) {
        transaction.run {
            userManager.validateDuplicateEmail(signUpCommand.email)
            userManager.register(signUpCommand)
        }
    }
}