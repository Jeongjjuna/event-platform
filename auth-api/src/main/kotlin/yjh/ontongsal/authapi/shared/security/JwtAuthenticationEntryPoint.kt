package yjh.ontongsal.authapi.shared.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import yjh.ontongsal.authapi.shared.response.ErrorResponse

private val log = KotlinLogging.logger {}

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: AuthenticationException,
    ) {
        log.warn { "[ERROR] ${e.message}" }

        val responseBody = ErrorResponse(
            code = HttpStatus.UNAUTHORIZED.value(),
            message = HttpStatus.UNAUTHORIZED.reasonPhrase,
            details = listOf()
        )

        response.apply {
            contentType = "application/json"
            characterEncoding = "UTF-8"
            status = HttpStatus.UNAUTHORIZED.value()
            writer.write(objectMapper.writeValueAsString(responseBody))
        }
    }
}