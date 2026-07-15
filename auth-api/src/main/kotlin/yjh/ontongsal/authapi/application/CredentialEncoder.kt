package yjh.ontongsal.authapi.application

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import yjh.ontongsal.authapi.domain.AuthErrorCode
import yjh.ontongsal.authapi.shared.response.AppException

@Component
class CredentialEncoder(
    private val passwordEncoder: PasswordEncoder,
) {
    fun hash(raw: String): String {
        return passwordEncoder.encode(raw)
            ?: throw AppException.Internal(AuthErrorCode.INTERNAL_SERVER_ERROR, "비밀번호 해시중 오류가 발생했습니다.")
    }

    fun matches(raw: String, hashed: String): Boolean =
        passwordEncoder.matches(raw, hashed)
}
