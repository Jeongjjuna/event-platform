package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.domain.ChangePasswordInfo
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

@Service
class ChangePasswordService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
) {

    fun changePassword(userId: Long, email: String, changePasswordInfo: ChangePasswordInfo) {
        transaction.run {
            userManager.changePassword(email, changePasswordInfo)
            tokenManager.deleteRefreshToken(userId)
        }
    }
}
