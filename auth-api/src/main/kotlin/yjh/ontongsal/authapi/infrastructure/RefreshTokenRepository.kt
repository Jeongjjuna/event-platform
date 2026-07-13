package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import yjh.ontongsal.authapi.domain.UserRefreshToken
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner
import java.time.Instant

@Repository
class RefreshTokenRepository(
    private val transaction: TransactionRunner
) {
    fun updateUserRefreshToken(userId: Long, refreshToken: String) = transaction.run {
        val updated = UserRefreshTokenTable
            .update({ UserRefreshTokenTable.userId eq userId }) {
                it[UserRefreshTokenTable.refreshToken] = refreshToken
                it[UserRefreshTokenTable.updatedAt] = Instant.now()
            }

        if (updated == 0) {
            UserRefreshTokenTable.insert {
                it[UserRefreshTokenTable.userId] = userId
                it[UserRefreshTokenTable.refreshToken] = refreshToken
                it[UserRefreshTokenTable.updatedAt] = Instant.now()
            }
        }
    }

    fun findByUserId(userId: Long): UserRefreshToken? = transaction.run {
        UserRefreshTokenTable
            .selectAll()
            .where { UserRefreshTokenTable.userId eq userId }
            .singleOrNull()
            ?.let {
                UserRefreshToken(
                    userId = it[UserRefreshTokenTable.userId],
                    refreshToken = it[UserRefreshTokenTable.refreshToken],
                    updatedAt = it[UserRefreshTokenTable.updatedAt]
                )
            }
    }
}