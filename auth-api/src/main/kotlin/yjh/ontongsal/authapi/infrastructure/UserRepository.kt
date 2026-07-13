package yjh.ontongsal.authapi.infrastructure

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import yjh.ontongsal.authapi.domain.User
import yjh.ontongsal.authapi.domain.UserRole
import yjh.ontongsal.authapi.shared.persistence.TransactionRunner

@Repository
class UserRepository(
    private val transaction: TransactionRunner
) {
    fun save(user: User) = transaction.run {
        UserTable.insert {
            it[email] = user.email
            it[password] = user.password
            it[userRole] = user.role.name
            it[createdAt] = user.createdAt
            it[updatedAt] = user.updatedAt
            it[deletedAt] = user.deletedAt
        }
    }

    fun findByEmail(email: String): User? = transaction.run {
        UserTable
            .selectAll()
            .where { (UserTable.email eq email) and UserTable.deletedAt.isNull() }
            .singleOrNull()
            ?.let {
                User(
                    id = it[UserTable.id],
                    email = it[UserTable.email],
                    password = it[UserTable.password],
                    role = UserRole.valueOf(it[UserTable.userRole]),
                    createdAt = it[UserTable.createdAt],
                    updatedAt = it[UserTable.updatedAt],
                    deletedAt = it[UserTable.deletedAt],
                )
            }
    }

    fun updatePassword(user: User): Int = transaction.run {
        UserTable
            .update({ (UserTable.id eq user.id!!) and UserTable.deletedAt.isNull() }) {
                it[UserTable.password] = user.password
                it[UserTable.updatedAt] = user.updatedAt
            }
    }

    fun softDelete(user: User): Int = transaction.run {
        UserTable
            .update({ (UserTable.id eq user.id!!) and UserTable.deletedAt.isNull() }) {
                it[UserTable.deletedAt] = user.deletedAt
                it[UserTable.updatedAt] = user.updatedAt
            }
    }
}
