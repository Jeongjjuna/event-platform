package yjh.ontongsal.authapi.support

import org.springframework.test.web.servlet.assertj.MvcTestResult
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper

inline fun <reified T> MvcTestResult.readBody(
    mapper: JsonMapper,
): T = mapper.readValue(
    response.contentAsString,
    object : TypeReference<T>() {}
)
