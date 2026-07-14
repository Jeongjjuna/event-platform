package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object UserTable : Table("users") {
    val id = long("id").autoIncrement()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val userRole = varchar("user_role", 20)
    val lastLoginAt = timestamp("last_login_at").nullable()

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}