package io.kotest.provided

import tools.jackson.databind.JsonNode

data class ApiSpec(
    val endpoint: String,
    val request: RequestSpec,
    val scenarios: MutableList<ApiScenario>
)

data class RequestSpec(
    val hasAuthorization: Boolean,
    val body: JsonNode?
)

data class ApiScenario(
    val description: String,
    val expectation: String,
    val response: ResponseSpec
)

data class ResponseSpec(
    val status: Int,
    val serviceName: String?,
    val body: JsonNode?
)