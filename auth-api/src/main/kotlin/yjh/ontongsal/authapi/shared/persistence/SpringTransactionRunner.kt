package yjh.ontongsal.authapi.shared.persistence

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class SpringTransactionRunner : TransactionRunner {

    @Transactional
    override fun <T> run(block: () -> T): T = block()

    @Transactional(readOnly = true)
    override fun <T> readOnly(block: () -> T): T = block()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun <T> runNew(block: () -> T): T = block()
}

