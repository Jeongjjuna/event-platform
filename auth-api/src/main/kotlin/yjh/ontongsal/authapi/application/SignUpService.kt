package yjh.ontongsal.authapi.application

import org.springframework.stereotype.Service
import yjh.ontongsal.authapi.domain.SignUpInfo
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

/**
 * 회원가입 "비즈니스 로직"
 */
@Service
class SignUpService(
    private val transaction: TransactionRunner,
    private val userManager: UserManager,
) {
    fun signUp(signUpInfo: SignUpInfo) {
        transaction.run {
            userManager.checkAlreadyRegistered(signUpInfo.email)
            userManager.signUp(signUpInfo)
        }
    }
}