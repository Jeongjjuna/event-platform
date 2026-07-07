package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val userRole = varchar("user_role", 20)

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}