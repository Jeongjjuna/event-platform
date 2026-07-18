package io.kotest.provided

import org.springframework.test.web.servlet.assertj.MvcTestResult

object ApiReportContext {

    private val context = ThreadLocal<MvcTestResult?>()

    fun record(result: MvcTestResult) {
        context.set(result)
    }

    fun get(): MvcTestResult? {
        return context.get()
    }

    fun clear() {
        context.remove()
    }
}