package yjh.ontongsal.authapi.application

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CredentialEncoder(
    private val passwordEncoder: PasswordEncoder,
) {
    fun hash(raw: String): String? =
        passwordEncoder.encode(raw)

    fun matches(raw: String, hashed: String): Boolean =
        passwordEncoder.matches(raw, hashed)
}
