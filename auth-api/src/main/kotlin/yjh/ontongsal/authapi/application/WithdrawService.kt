package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

@Service
class WithdrawService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    private val userCache: UserCache,
) {

    fun withdraw(userId: Long, email: String) {
        transaction.run {
            userManager.withdraw(email)
            tokenManager.deleteRefreshToken(userId)
        }
        userCache.evict(email)
    }
}
