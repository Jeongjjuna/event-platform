package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object UserSessionTable: Table("user_sessions") {
    val userId = long("user_id")
    val refreshToken = varchar("refresh_token", 1024)

    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(userId)
}