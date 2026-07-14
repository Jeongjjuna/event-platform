package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import yjh.ontongsal.authapi.domain.JwtToken
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserSession
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

@Repository
class UserSessionRepository(
    private val transaction: TransactionRunner
) {
    fun updateRefreshToken(user: User, jwtToken: JwtToken) = transaction.run {
        val updated = UserSessionTable
            .update({ UserSessionTable.userId eq user.id }) {
                it[UserSessionTable.refreshToken] = jwtToken.refreshToken
                it[UserSessionTable.updatedAt] = user.updatedAt
            }

        if (updated == 0) {
            UserSessionTable.insert {
                it[UserSessionTable.userId] = userId
                it[UserSessionTable.refreshToken] = refreshToken
                it[UserSessionTable.updatedAt] = user.updatedAt
            }
        }
    }

    fun findByUserId(userId: Long): UserSession? = transaction.run {
        UserSessionTable
            .selectAll()
            .where { UserSessionTable.userId eq userId }
            .singleOrNull()
            ?.let {
                UserSession(
                    userId = it[UserSessionTable.userId],
                    refreshToken = it[UserSessionTable.refreshToken],
                    updatedAt = it[UserSessionTable.updatedAt]
                )
            }
    }

    fun deleteByUserId(userId: Long) = transaction.run {
        UserSessionTable
            .deleteWhere { UserSessionTable.userId eq userId }
    }

}