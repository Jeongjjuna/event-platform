package yjh.ontongsal.authapi.shared.security.jwt

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

/**
 * Authorization 의 Authorization 헤더 정보가 있다면 SecurityContextHolder 에 인증 정보를 담아준다.
 */
@Component
class JwtSecurityContextFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token: String? = resolveToken(request)

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            try {
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.setContext(context)
            } catch (e: InvalidJwtException) {

                log.debug { "Invalid JWT token : ${e.message}" }
                // 인증 실패 → 그냥 통과 (401은 EntryPoint가 처리)
            } catch (e: Exception) {
                log.debug { "JWT token 검증 에러" }
                // 인증 실패 → 그냥 통과 (401은 EntryPoint가 처리)
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        return request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
    }
}