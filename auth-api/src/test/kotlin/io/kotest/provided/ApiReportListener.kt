package io.kotest.provided

import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.test.TestResult


class ApiReportListener : TestListener, AfterProjectListener {

    private val reports = mutableListOf<ScenarioReport>()

    override suspend fun afterProject() {
        println("총 ${reports.size}개의 API 테스트")
        reports.forEach(::printReport)
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        if (testCase.type != TestType.Test) {
            return
        }

        val report = createScenarioReport(testCase, result)

        synchronized(reports) {
            reports += report
        }
    }

    private fun createScenarioReport(
        testCase: TestCase,
        result: TestResult,
    ): ScenarioReport {

        val names = buildHierarchy(testCase)

        return ScenarioReport(
            describe = names.firstOrNull().orEmpty(),
            context = names
                .drop(1)
                .dropLast(1)
                .joinToString(" "),
            it = names.lastOrNull().orEmpty(),
            status = if (result.isSuccess) 200 else 500,
            duration = result.duration.inWholeMilliseconds,
            request = null,
            response = null,
        )
    }

    private fun buildHierarchy(testCase: TestCase): List<String> {
        val names = mutableListOf<String>()

        var current: TestCase? = testCase

        while (current != null) {
            names.add(0, current.name.name)
            current = current.parent
        }

        return names
    }

    private fun printReport(report: ScenarioReport) {
        println(
            """
            ========================================
            Describe : ${report.describe}
            Context  : ${report.context}
            It       : ${report.it}
            Status   : ${report.status}
            Duration : ${report.duration} ms
            ========================================
            """.trimIndent()
        )
    }
}