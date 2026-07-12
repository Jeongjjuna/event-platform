package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object UserRefreshTokenTable: Table("user_refresh_token") {
    val userId = long("user_id")
    val refreshToken = varchar("refresh_token", 100)

    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(userId)
}