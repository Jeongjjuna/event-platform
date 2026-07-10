package yjh.ontongsal.authapi.shared.web

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode

object SensitiveDataMasker {

    private const val MASK = "*****"

    private val FULL_MASK_BODY_KEYS = setOf(
        "password",
    )

    private val PARTIAL_MASK_BODY_RULES: Map<String, (String) -> String> = mapOf(
        "accesstoken" to ::maskTail,
        "refreshtoken" to ::maskTail,
        "phone" to ::maskPhone,
        "email" to ::maskEmail,
    )

    private val FULL_MASK_HEADERS = setOf(
        "cookie",
        "set-cookie",
        "x-api-key",
        "proxy-authorization",
    )

    private val PARTIAL_MASK_HEADER_RULES: Map<String, (String) -> String> = mapOf(
        "authorization" to ::maskAuthorization,
        "x-auth-token" to ::maskTail,
    )

    fun maskBody(node: JsonNode): JsonNode {
        when (node) {
            is ObjectNode -> {
                node.propertyNames().toList().forEach { name ->
                    when (val key = name.lowercase()) {
                        in FULL_MASK_BODY_KEYS ->
                            node.put(name, MASK)

                        in PARTIAL_MASK_BODY_RULES ->
                            node.put(name, applyPartial(PARTIAL_MASK_BODY_RULES.getValue(key), node.get(name)))

                        else -> maskBody(node.get(name))
                    }
                }
            }

            is ArrayNode -> node.values().forEach { maskBody(it) }

            else -> {}
        }

        return node
    }

    fun maskHeaders(headers: Map<String, String>): Map<String, String> =
        headers.mapValues { (name, value) -> maskHeader(name, value) }

    fun maskHeader(name: String, value: String): String {
        val key = name.lowercase()
        return when {
            key in FULL_MASK_HEADERS -> MASK
            key in PARTIAL_MASK_HEADER_RULES -> PARTIAL_MASK_HEADER_RULES.getValue(key)(value)
            else -> value
        }
    }

    // 문자열이 아닌 값에 부분 마스킹을 적용하면 어설프게 노출될 수 있어 완전 마스킹으로 폴백
    private fun applyPartial(rule: (String) -> String, node: JsonNode): String =
        if (node.isString) rule(node.asString()) else MASK

    private fun maskTail(value: String): String =
        if (value.length > 4) "${value.take(4)}$MASK" else MASK

    private fun maskAuthorization(value: String): String =
        value.split(" ", limit = 2)
            .takeIf { it.size == 2 }
            ?.let { (scheme, token) -> "$scheme ${maskEdges(token)}" }
            ?: MASK

    // 노출량(앞4+뒤3)이 원문 길이 이상이면 사실상 전체 노출이라 완전 마스킹
    private fun maskEdges(value: String): String =
        if (value.length > 7) "${value.take(4)}$MASK${value.takeLast(3)}"
        else MASK

    /** "010-1234-5678" → "010-****-5678" */
    private fun maskPhone(value: String): String =
        value.replace(Regex("(\\d{3})-?(\\d{3,4})-?(\\d{4})"), "$1-****-$3")

    /** "hong@example.com" → "ho***@example.com" */
    private fun maskEmail(value: String): String =
        value.replace(Regex("(^[^@]{2})[^@]*(@.*)"), "$1***$2")
}