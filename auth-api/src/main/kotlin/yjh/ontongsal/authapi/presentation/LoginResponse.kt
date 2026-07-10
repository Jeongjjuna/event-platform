package yjh.ontongsal.authapi.presentation

import yjh.ontongsal.authapi.domain.LoginResult
import yjh.ontongsal.authapi.domain.UserRole
import kotlin.time.Instant

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
) {

    data class UserResponse(
        val id: Int,
        val email: String,
        val role: UserRole,
        val registeredAt: Instant,
    )

    companion object {
        fun from(loginResult: LoginResult) = LoginResponse(
            accessToken = loginResult.accessToken,
            refreshToken = loginResult.refreshToken,
            user = UserResponse(
                id = loginResult.user.id!!,
                email = loginResult.user.email,
                role = loginResult.user.role,
                registeredAt = loginResult.user.createdAt
            )
        )
    }
}
