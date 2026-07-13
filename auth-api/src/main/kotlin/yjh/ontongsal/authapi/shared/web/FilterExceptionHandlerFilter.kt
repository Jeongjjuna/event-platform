package yjh.ontongsal.authapi.shared.web

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper
import yjh.ontongsal.authapi.shared.response.ErrorResponse

private val log = KotlinLogging.logger {}

@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@Component
class FilterExceptionHandlerFilter(
    private val objectMapper: ObjectMapper,
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            log.error(e) { "[Filter] 필터 체인 예외: ${request.method} ${request.requestURI}" }

            if (response.isCommitted) { // 이미 나간 응답은 못 고침 → 시도 자체를 포기
                throw e
            }
            response.resetBuffer() // 버퍼(body)만 비움 — 상태코드와 헤더는 유지 → 쓰다 만 응답데이터 청소

            val responseBody = ErrorResponse(
                code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                details = listOf()
            )

            response.apply {
                contentType = "application/json"
                characterEncoding = "UTF-8"
                status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                writer.write(objectMapper.writeValueAsString(responseBody))
            }
        }
    }

}