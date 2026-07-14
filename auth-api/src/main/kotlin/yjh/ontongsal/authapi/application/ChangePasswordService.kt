package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.application.command.ChangePasswordCommand
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

/**
 * 비밀번호 변경 "비즈니스 로직"
 */
@Service
class ChangePasswordService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
    private val sessionManager: SessionManager,
    private val userReader: UserReader
) {

    fun changePassword(userId: Long, email: String, changePasswordCommand: ChangePasswordCommand) {
        transaction.run {
            val user = userReader.read(email)
            userManager.changePassword(user, changePasswordCommand)

            sessionManager.deleteSession(userId)
        }
    }
}
