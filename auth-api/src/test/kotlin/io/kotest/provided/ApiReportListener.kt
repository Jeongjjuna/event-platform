package io.kotest.provided

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.test.TestResult
import org.springframework.test.web.servlet.assertj.MvcTestResult
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jsonMapper
import java.io.File
import java.util.concurrent.ConcurrentHashMap


class ApiReportListener : TestListener, AfterProjectListener {

    private val specs = ConcurrentHashMap<String, ApiSpec>()
    private val yamlMapper = YAMLMapper()

    override suspend fun afterProject() {

        val dir = File("build/kotest-api-docs")
        dir.mkdirs()

        val yamlFile = File(dir, "api-spec.yaml")
        yamlMapper.writeValue(yamlFile, specs)

        val markdownFile = File(dir, "api-spec.md")
        markdownFile.writeText(generateMarkdown(specs))

        println(
            """
            ========================================
             API Specification Generated
            ========================================
             YAML     : ${yamlFile.path}
             Markdown : ${markdownFile.path}
            ========================================
            """.trimIndent()
        )
    }

    private fun generateMarkdown(
        specs: Map<String, ApiSpec>
    ): String {
        return buildString {
            appendLine("# API Specification")
            appendLine()
            specs.values.forEach { api ->
                appendLine(
                    "## ${api.endpoint}"
                )
                appendLine()
                appendLine("## Request")
                appendLine()
                appendLine("### Header")
                appendLine()
                appendLine(
                    "| Header | Required |"
                )
                appendLine(
                    "|---|---|"
                )
                appendLine(
                    "| Authorization | ${api.request.hasAuthorization} |"
                )
                appendLine()
                appendLine("### Body")
                appendLine()
                appendJson(
                    api.request.body
                )
                appendLine()
                appendLine("## Response")
                appendLine()
                api.scenarios.forEach { scenario ->
                    appendLine("### ${scenario.description}")
                    appendLine()
                    appendLine("**Expected**")
                    appendLine()
                    appendLine(scenario.expectation)
                    appendLine()
                    appendLine("| Status | Service |")
                    appendLine("|---|---|")
                    appendLine("| ${scenario.response.status} | ${scenario.response.serviceName} |")
                    appendLine()
                    appendLine("Body")
                    appendJson(scenario.response.body)
                    appendLine()
                    appendLine("---")
                    appendLine()
                }
            }
        }
    }

    private fun StringBuilder.appendJson(
        body: JsonNode?
    ) {
        if (body == null) {
            appendLine("없음")
            return
        }

        appendLine("```json")
        appendLine(
            body.toPrettyString()
        )
        appendLine("```")
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        if (testCase.type != TestType.Test) {
            return
        }

        val mvcTestResult = ApiReportContext.get() ?: return
        val names = buildHierarchy(testCase)
        val endpoint = names.first()
        val scenario = createScenario(
            testCase,
            mvcTestResult
        )

        specs.compute(
            endpoint
        ) { _, existing ->
            val apiSpec = existing ?: ApiSpec(
                        endpoint = endpoint,
                        request = createRequestSpec(mvcTestResult),
                        scenarios = mutableListOf()
                    )
            apiSpec.scenarios.add(scenario)
            apiSpec
        }
    }

    private fun createScenario(
        testCase: TestCase,
        mvcTestResult: MvcTestResult,
    ): ApiScenario {
        val names = buildHierarchy(testCase)
        val response = mvcTestResult.mvcResult.response

        return ApiScenario(
            description = names
                .drop(1)
                .dropLast(1)
                .joinToString(" "),
            expectation = names.last(),
            response = ResponseSpec(
                    status = response.status,
                    serviceName = response.getHeader("X-Service-Name"),
                    body = parseJson(response.contentAsString)
                )
        )
    }

    private fun createRequestSpec(
        mvcTestResult: MvcTestResult
    ): RequestSpec {
        val request = mvcTestResult.mvcResult.request
        return RequestSpec(
            hasAuthorization = request.getHeader("Authorization") != null,
            body = parseJson(request.contentAsString)
        )
    }

    private fun parseJson(
        value: String?
    ): JsonNode? {
        if (value.isNullOrBlank()) {
            return null
        }
        return jsonMapper().readTree(value)
    }

    private fun buildHierarchy(
        testCase: TestCase
    ): List<String> {
        val names = mutableListOf<String>()
        var current: TestCase? = testCase

        while (current != null) {
            names.add(0, current.name.name)
            current = current.parent
        }

        return names
    }
}