package yjh.ontongsal.authapi.shared.persistence

interface TransactionRunner {
    fun <T> run(block: () -> T): T
    fun <T> readOnly(block: () -> T): T
    fun <T> runNew(block: () -> T): T
}
