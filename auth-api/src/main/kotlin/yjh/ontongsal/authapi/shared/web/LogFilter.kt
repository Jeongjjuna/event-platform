package yjh.ontongsal.authapi.shared.web

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import tools.jackson.databind.json.JsonMapper
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Component
class LogFilter : OncePerRequestFilter() {

    companion object {
        const val EMPTY = "-"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val reqWrapper = CachedBodyHttpServletRequest(request)
        val resWrapper = ContentCachingResponseWrapper(response)

        // chain 진입 전에 request 로깅 (body 는 wrapper 생성 시 이미 캐싱됨)
        logRequest(reqWrapper)

        filterChain.doFilter(reqWrapper, resWrapper)

        logResponse(reqWrapper, resWrapper)

        // body 복원
        resWrapper.copyBodyToResponse()
    }

    private fun logRequest(req: CachedBodyHttpServletRequest) {
        val method = req.method
        val uri = req.requestURI

        val queryParams = req.queryString ?: EMPTY
        val headers = getHeaders(req)
        val requestBody = getBody(req)

        val logMap = mapOf(
            "type" to "HTTP Request",
            "method" to method,
            "uri" to uri,
            "query" to queryParams,
            "body" to requestBody,
            "headers" to headers,
        )
        log.info { logMap }
    }

    /**
     * HTTP 응답정보는 민감정보가 없을것으로 약속하고 전부 로깅한다.
     */
    private fun logResponse(req: CachedBodyHttpServletRequest, res: ContentCachingResponseWrapper) {
        val status = res.status
        val method = req.method
        val uri = req.requestURI

        val responseBody = String(res.contentAsByteArray, StandardCharsets.UTF_8)
            .ifBlank { EMPTY }

        val logMap = mapOf(
            "type" to "HTTP Response",
            "status" to status,
            "method" to method,
            "uri" to uri,
            "body" to responseBody
        )
        log.info { logMap }
    }

    private fun getHeaders(request: HttpServletRequest): Map<String, String> {
        val names = request.headerNames ?: return emptyMap()
        val map = mutableMapOf<String, String>()

        while (names.hasMoreElements()) {
            val name = names.nextElement()
            map[name] = SensitiveDataMasker.maskHeader(name, request.getHeader(name))
        }

        return map
    }

    private fun getBody(req: CachedBodyHttpServletRequest): String {
        return String(req.getBody(), StandardCharsets.UTF_8)
            .let { raw ->
                runCatching {
                    SensitiveDataMasker.maskBody(JsonMapper.shared().readTree(raw)).toString()
                }.getOrDefault(raw)
            }
            .ifBlank { EMPTY }
    }
}