package yjh.ontongsal.authapi.presentation

import yjh.ontongsal.authapi.domain.User
import kotlin.time.Instant

data class MyInfoResponse(
    val userId: Int,
    val email: String,
    val role: String,
    val registeredAt: Instant,
) {
    companion object {
        fun from(user: User): MyInfoResponse {
            return MyInfoResponse(
                userId = user.id!!,
                email = user.email,
                role = user.role.name,
                registeredAt = user.createdAt,
            )
        }
    }

}
