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

            appendLine("# [AUTH API] API Specification")
            appendLine()

            specs.values.forEach { api ->
                appendLine("<details>")
                appendLine()
                appendLine(
                    """
                <summary style="
                    background:${httpMethodColor(api.endpoint)};
                    padding:16px;
                    border-radius:10px;
                    margin-bottom:16px;
                    cursor:pointer;
                    font-size:22px;
                    font-weight:700;
                    color:#1e293b;
                ">
                """.trimIndent()
                )
                appendLine("📌 ${api.endpoint}")
                appendLine("</summary>")
                appendLine()

                // API 컴포넌트 내부 시작
                appendLine("### Request")
                appendLine()
                appendLine("#### Headers")

                appendLine("| Header | Required |")
                appendLine("|---|---|")
                appendLine("| Authorization | ${api.request.hasAuthorization.toString().uppercase()} |")
                appendLine()

                appendLine("#### Body")
                appendLine()
                appendJson(api.request.body)
                appendLine()

                appendLine("---")
                appendLine()

                appendLine("### Response")
                appendLine()

                // Response 컴포넌트 내부 시작
                api.scenarios.forEach { scenario ->
                    appendLine("<details>")
                    appendLine()
                    appendLine(
                        """
                    <summary style="
                        background:#646f7b;
                        padding:12px;
                        border-radius:4px;
                        margin-bottom:8px;
                        cursor:pointer;
                        font-size:17px;
                        font-weight:600;
                    ">
                    """.trimIndent()
                    )
                    appendLine("🧊 ${scenario.response.status} - ${scenario.description}")
                    appendLine("</summary>")
                    appendLine()

                    appendLine("> **Expected**")
                    appendLine(">")
                    appendLine("> ${scenario.expectation}")
                    appendLine()

                    appendLine("| 항목 | 값 |")
                    appendLine("|---|---|")
                    appendLine("| HTTP Status | `${scenario.response.status}` |")
                    appendLine("| X-Service-Name | ${scenario.response.serviceName ?: "-"} |")
                    appendLine()

                    appendLine("#### Response Body")
                    appendJson(scenario.response.body)
                    appendLine()
                    appendLine("</details>")
                    appendLine()
                }

                appendLine("</details>")
                appendLine()
            }
        }
    }

    private fun httpMethodColor(endpoint: String): String {
        return when {
            endpoint.startsWith("GET") -> "#dcfce7"     // 연한 초록
            endpoint.startsWith("POST") -> "#dbeafe"    // 연한 파랑
            endpoint.startsWith("PATCH") -> "#fef3c7"   // 연한 노랑
            endpoint.startsWith("DELETE") -> "#fee2e2"  // 연한 빨강
            else -> "#e2e8f0"
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