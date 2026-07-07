package yjh.ontongsal.authapi.shared.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class MdcFilter(
    @Value($$"${spring.application.name}")
    private val applicationName: String,
) : OncePerRequestFilter() {

    companion object {
        const val TRACE_ID = "traceId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId = UUID.randomUUID().toString()
        MDC.put(TRACE_ID, traceId)

        response.setHeader("X-Service-Name", applicationName)
        filterChain.doFilter(request, response)

        MDC.remove(TRACE_ID)
    }
}
