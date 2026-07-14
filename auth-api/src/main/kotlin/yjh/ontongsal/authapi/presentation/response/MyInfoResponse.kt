package yjh.ontongsal.authapi.presentation.response

import yjh.ontongsal.authapi.domain.CachedUser
import yjh.ontongsal.authapi.domain.User
import java.time.Instant

data class MyInfoResponse(
    val userId: Long,
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

        fun from(cachedUser: CachedUser): MyInfoResponse {
            return MyInfoResponse(
                userId = cachedUser.id,
                email = cachedUser.email,
                role = cachedUser.role.name,
                registeredAt = cachedUser.createdAt,
            )
        }
    }

}
