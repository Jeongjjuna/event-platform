package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

@Service
class WithdrawService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
    private val sessionManager: SessionManager,
    private val userReader: UserReader,
    private val userCache: UserCache,
) {

    fun withdraw(userId: Long, email: String) {
        transaction.run {
            val user = userReader.read(email)
            user.withdraw()

            userManager.withdraw(user)
            sessionManager.deleteSession(userId)
        }
        userCache.evict(email)
    }
}
