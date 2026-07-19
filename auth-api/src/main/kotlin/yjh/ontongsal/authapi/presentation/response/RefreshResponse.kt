package yjh.ontongsal.authapi.presentation.response

import yjh.ontongsal.authapi.domain.RefreshResult
import yjh.ontongsal.authapi.domain.UserRole
import java.time.Instant

data class RefreshResponse(
    val accessToken: String,
    val user: UserResponse,
) {
    data class UserResponse(
        val id: Long,
        val email: String,
        val role: UserRole,
        val registeredAt: Instant,
    )

    companion object {
        fun from(refreshResult: RefreshResult) = RefreshResponse(
            accessToken = refreshResult.jwtToken.accessToken,
            user = UserResponse(
                id = refreshResult.user.id,
                email = refreshResult.user.email,
                role = refreshResult.user.role,
                registeredAt = refreshResult.user.createdAt
            )
        )
    }
}